[CmdletBinding()]
param(
    [string]$Profile = "project-testing",
    [string]$Region = "ap-south-1",
    [string]$BootstrapStackName = "flatmate-github-oidc",
    [string]$StackName = "flatmate-testing",
    [string]$DynamoDbTableName = "FlatmateData",
    [string]$AllowedOrigins = "http://localhost:3000,http://localhost:5173",
    [string]$RuntimeParameterName = "/flatmate/testing/runtime-env",
    [switch]$CreateDynamoDbTable
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $PSScriptRoot

function Invoke-Aws {
    param([Parameter(Mandatory)][string[]]$Arguments)
    $result = & aws @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "AWS CLI command failed: aws $($Arguments -join ' ')"
    }
    return $result
}

function Get-StackOutput {
    param([string]$OutputKey)
    return (Invoke-Aws -Arguments @(
        "cloudformation", "describe-stacks",
        "--stack-name", $StackName,
        "--region", $Region,
        "--profile", $Profile,
        "--query", "Stacks[0].Outputs[?OutputKey=='$OutputKey'].OutputValue",
        "--output", "text"
    )).Trim()
}

function ConvertTo-BashSingleQuoted {
    param([string]$Value)
    return "'" + $Value.Replace("'", "'`"'`"'") + "'"
}

if (-not (Get-Command aws -ErrorAction SilentlyContinue)) {
    throw "AWS CLI v2 is required. Install it, reopen PowerShell, and run this script again."
}
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    throw "Maven is required for the one-time local deployment."
}
if (-not (Get-Command tar.exe -ErrorAction SilentlyContinue)) {
    throw "Windows tar.exe is required to create the deployment package."
}

Push-Location $ProjectRoot
try {
    Write-Host "Verifying the AWS SSO session..." -ForegroundColor Cyan
    Invoke-Aws -Arguments @("sts", "get-caller-identity", "--profile", $Profile, "--region", $Region) | Out-Null

    $runtimeType = & aws ssm get-parameter `
        --name $RuntimeParameterName `
        --region $Region `
        --profile $Profile `
        --query "Parameter.Type" `
        --output text 2>$null
    if ($LASTEXITCODE -ne 0 -or $runtimeType -ne "SecureString") {
        throw "Create the SSM SecureString '$RuntimeParameterName' before deployment. It must contain a JWT_SECRET=... line and must not contain AWS access keys."
    }

    $tableStatus = & aws dynamodb describe-table `
        --table-name $DynamoDbTableName `
        --region $Region `
        --profile $Profile `
        --query "Table.TableStatus" `
        --output text 2>$null

    if ($LASTEXITCODE -ne 0) {
        if (-not $CreateDynamoDbTable) {
            throw "DynamoDB table '$DynamoDbTableName' does not exist. Re-run with -CreateDynamoDbTable only after confirming this is the new testing account."
        }

        Write-Host "Creating the explicitly requested on-demand DynamoDB table..." -ForegroundColor Yellow
        Invoke-Aws -Arguments @(
            "dynamodb", "create-table",
            "--table-name", $DynamoDbTableName,
            "--attribute-definitions", "AttributeName=PK,AttributeType=S", "AttributeName=SK,AttributeType=S", "AttributeName=listingId,AttributeType=S",
            "--key-schema", "AttributeName=PK,KeyType=HASH", "AttributeName=SK,KeyType=RANGE",
            "--global-secondary-indexes", "IndexName=ListingIndex,KeySchema=[{AttributeName=listingId,KeyType=HASH}],Projection={ProjectionType=ALL}",
            "--billing-mode", "PAY_PER_REQUEST",
            "--sse-specification", "Enabled=true",
            "--region", $Region,
            "--profile", $Profile
        ) | Out-Null
        Invoke-Aws -Arguments @("dynamodb", "wait", "table-exists", "--table-name", $DynamoDbTableName, "--region", $Region, "--profile", $Profile) | Out-Null
    }

    $existingBootstrapSetting = & aws cloudformation describe-stacks `
        --stack-name $BootstrapStackName `
        --region $Region `
        --profile $Profile `
        --query "Stacks[0].Parameters[?ParameterKey=='CreateOidcProvider'].ParameterValue" `
        --output text 2>$null

    if ($LASTEXITCODE -eq 0 -and $existingBootstrapSetting -in @("true", "false")) {
        # Preserve stack ownership on every re-run. Switching true to false would
        # delete an OIDC provider that this bootstrap stack originally created.
        $createOidcProvider = $existingBootstrapSetting
    } else {
        $oidcCount = Invoke-Aws -Arguments @(
            "iam", "list-open-id-connect-providers",
            "--profile", $Profile,
            "--query", "length(OpenIDConnectProviderList[?contains(Arn, 'token.actions.githubusercontent.com')])",
            "--output", "text"
        )
        $createOidcProvider = if ([int]$oidcCount -eq 0) { "true" } else { "false" }
    }

    Write-Host "Creating the GitHub OIDC and CloudFormation roles..." -ForegroundColor Cyan
    Invoke-Aws -Arguments @(
        "cloudformation", "deploy",
        "--template-file", "github-oidc-deploy-role.yaml",
        "--stack-name", $BootstrapStackName,
        "--capabilities", "CAPABILITY_NAMED_IAM",
        "--no-fail-on-empty-changeset",
        "--parameter-overrides",
        "GitHubOwner=rajasekar-cloud",
        "GitHubRepository=flat_in_mate",
        "DeploymentBranch=main",
        "ApplicationStackName=$StackName",
        "CreateOidcProvider=$createOidcProvider",
        "--region", $Region,
        "--profile", $Profile
    ) | Out-Host

    $cloudFormationRoleArn = (Invoke-Aws -Arguments @(
        "cloudformation", "describe-stacks", "--stack-name", $BootstrapStackName,
        "--query", "Stacks[0].Outputs[?OutputKey=='CloudFormationExecutionRoleArn'].OutputValue",
        "--output", "text", "--region", $Region, "--profile", $Profile
    )).Trim()

    $githubRoleArn = (Invoke-Aws -Arguments @(
        "cloudformation", "describe-stacks", "--stack-name", $BootstrapStackName,
        "--query", "Stacks[0].Outputs[?OutputKey=='GitHubRoleArn'].OutputValue",
        "--output", "text", "--region", $Region, "--profile", $Profile
    )).Trim()

    $vpcId = (Invoke-Aws -Arguments @(
        "ec2", "describe-vpcs", "--filters", "Name=is-default,Values=true",
        "--query", "Vpcs[0].VpcId", "--output", "text", "--region", $Region, "--profile", $Profile
    )).Trim()
    if (-not $vpcId -or $vpcId -eq "None") {
        throw "No default VPC exists in $Region. This low-cost stack intentionally does not create a VPC."
    }

    $subnetId = (Invoke-Aws -Arguments @(
        "ec2", "describe-subnets", "--filters", "Name=vpc-id,Values=$vpcId", "Name=default-for-az,Values=true",
        "--query", "sort_by(Subnets,&AvailabilityZone)[0].SubnetId", "--output", "text", "--region", $Region, "--profile", $Profile
    )).Trim()
    $prefixListId = (Invoke-Aws -Arguments @(
        "ec2", "describe-managed-prefix-lists", "--filters", "Name=prefix-list-name,Values=com.amazonaws.global.cloudfront.origin-facing",
        "--query", "PrefixLists[0].PrefixListId", "--output", "text", "--region", $Region, "--profile", $Profile
    )).Trim()

    Write-Host "Deploying the testing infrastructure..." -ForegroundColor Cyan
    Invoke-Aws -Arguments @(
        "cloudformation", "deploy",
        "--template-file", "template.yaml",
        "--stack-name", $StackName,
        "--role-arn", $cloudFormationRoleArn,
        "--capabilities", "CAPABILITY_NAMED_IAM",
        "--no-fail-on-empty-changeset",
        "--parameter-overrides",
        "EnvironmentName=testing",
        "VpcId=$vpcId",
        "SubnetId=$subnetId",
        "CloudFrontOriginPrefixListId=$prefixListId",
        "DynamoDbTableName=$DynamoDbTableName",
        "AllowedOrigins=$AllowedOrigins",
        "RuntimeSecretsParameterName=$RuntimeParameterName",
        "--region", $Region,
        "--profile", $Profile
    ) | Out-Host

    Write-Host "Running tests and building the application..." -ForegroundColor Cyan
    & mvn --batch-mode test
    if ($LASTEXITCODE -ne 0) { throw "Maven tests failed." }
    & mvn --batch-mode package -DskipTests
    if ($LASTEXITCODE -ne 0) { throw "Maven packaging failed." }

    $instanceId = Get-StackOutput "InstanceId"
    $deploymentBucket = Get-StackOutput "DeploymentBucketName"
    $mediaBucket = Get-StackOutput "MediaBucketName"
    $logGroup = Get-StackOutput "ApplicationLogGroupName"
    $healthUrl = Get-StackOutput "HealthUrl"
    $baseUrl = Get-StackOutput "PublicBaseUrl"

    $releaseId = (git rev-parse HEAD).Trim()
    if ($LASTEXITCODE -ne 0) { $releaseId = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds().ToString() }
    $artifactName = "flatmate-$releaseId.tar.gz"
    $artifactPath = Join-Path $env:TEMP $artifactName
    if (Test-Path $artifactPath) { Remove-Item -LiteralPath $artifactPath -Force }

    & tar.exe -czf $artifactPath target/*.jar Dockerfile docker-compose.yml nginx scripts/remote-deploy.sh scripts/remote-rollback.sh
    if ($LASTEXITCODE -ne 0) { throw "Could not create the deployment archive." }

    Invoke-Aws -Arguments @(
        "s3", "cp", $artifactPath, "s3://$deploymentBucket/releases/$artifactName",
        "--only-show-errors", "--region", $Region, "--profile", $Profile
    ) | Out-Null

    Write-Host "Waiting for the instance to register with Systems Manager..." -ForegroundColor Cyan
    $online = $false
    for ($attempt = 1; $attempt -le 30; $attempt++) {
        $pingStatus = & aws ssm describe-instance-information `
            --filters "Key=InstanceIds,Values=$instanceId" `
            --query "InstanceInformationList[0].PingStatus" `
            --output text --region $Region --profile $Profile 2>$null
        if ($LASTEXITCODE -eq 0 -and $pingStatus -eq "Online") {
            $online = $true
            break
        }
        Start-Sleep -Seconds 10
    }
    if (-not $online) { throw "The instance did not become available through Systems Manager." }

    $releaseDirectory = "/opt/flatmate/releases/$releaseId"
    $remoteArtifact = "/tmp/$artifactName"
    $remoteDeploy = @(
        "sudo", "bash", "$releaseDirectory/scripts/remote-deploy.sh",
        $Region, $DynamoDbTableName, $mediaBucket, $RuntimeParameterName,
        $AllowedOrigins, $logGroup, $releaseDirectory
    ) | ForEach-Object { ConvertTo-BashSingleQuoted $_ }

    $commands = @(
        "set -euo pipefail",
        "sudo install -d -m 0750 $(ConvertTo-BashSingleQuoted $releaseDirectory)",
        "aws s3 cp $(ConvertTo-BashSingleQuoted "s3://$deploymentBucket/releases/$artifactName") $(ConvertTo-BashSingleQuoted $remoteArtifact) --region $(ConvertTo-BashSingleQuoted $Region)",
        "sudo tar -xzf $(ConvertTo-BashSingleQuoted $remoteArtifact) -C $(ConvertTo-BashSingleQuoted $releaseDirectory)",
        "sudo chmod 0755 $(ConvertTo-BashSingleQuoted "$releaseDirectory/scripts/remote-deploy.sh")",
        ($remoteDeploy -join " "),
        "rm -f $(ConvertTo-BashSingleQuoted $remoteArtifact)"
    )

    $parametersPath = Join-Path $env:TEMP "flatmate-ssm-parameters.json"
    $parametersJson = @{ commands = $commands } | ConvertTo-Json -Depth 4
    [System.IO.File]::WriteAllText($parametersPath, $parametersJson, [System.Text.UTF8Encoding]::new($false))

    $commandId = (Invoke-Aws -Arguments @(
        "ssm", "send-command", "--instance-ids", $instanceId,
        "--document-name", "AWS-RunShellScript",
        "--comment", "Initial Flatmate deployment $releaseId",
        "--parameters", "file://$parametersPath",
        "--query", "Command.CommandId", "--output", "text",
        "--region", $Region, "--profile", $Profile
    )).Trim()

    Invoke-Aws -Arguments @(
        "ssm", "wait", "command-executed", "--command-id", $commandId,
        "--instance-id", $instanceId, "--region", $Region, "--profile", $Profile
    ) | Out-Null

    Invoke-Aws -Arguments @(
        "ssm", "get-command-invocation", "--command-id", $commandId,
        "--instance-id", $instanceId,
        "--query", "{Status:Status,Output:StandardOutputContent,Error:StandardErrorContent}",
        "--region", $Region, "--profile", $Profile
    ) | Out-Host

    $healthy = $false
    for ($attempt = 1; $attempt -le 30; $attempt++) {
        try {
            $response = Invoke-WebRequest -Uri $healthUrl -UseBasicParsing -TimeoutSec 15
            if ($response.StatusCode -eq 200) { $healthy = $true; break }
        } catch {
            Start-Sleep -Seconds 10
        }
    }
    if (-not $healthy) { throw "The public health endpoint did not become healthy: $healthUrl" }

    Write-Host "Deployment completed successfully." -ForegroundColor Green
    Write-Host "Base URL: $baseUrl"
    Write-Host "Health URL: $healthUrl"
    Write-Host "GitHub variable AWS_ROLE_ARN: $githubRoleArn"
    Write-Host "GitHub variable CLOUDFORMATION_ROLE_ARN: $cloudFormationRoleArn"
} finally {
    if ($artifactPath -and (Test-Path $artifactPath)) { Remove-Item -LiteralPath $artifactPath -Force }
    if ($parametersPath -and (Test-Path $parametersPath)) { Remove-Item -LiteralPath $parametersPath -Force }
    Pop-Location
}
