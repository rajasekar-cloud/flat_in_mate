[CmdletBinding()]
param(
    [string]$Profile = "project-testing",
    [string]$Region = "ap-south-1",
    [string]$StackName = "flatmate-testing",
    [string]$BootstrapStackName = "flatmate-github-oidc"
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $PSScriptRoot

function Get-StackParameter {
    param([string]$Key)
    return (& aws cloudformation describe-stacks --stack-name $StackName --region $Region --profile $Profile `
        --query "Stacks[0].Parameters[?ParameterKey=='$Key'].ParameterValue" --output text).Trim()
}

$instanceId = (& aws cloudformation describe-stacks --stack-name $StackName --region $Region --profile $Profile `
    --query "Stacks[0].Outputs[?OutputKey=='InstanceId'].OutputValue" --output text).Trim()
if ($LASTEXITCODE -ne 0 -or -not $instanceId) { throw "Could not find the testing instance." }

& aws ec2 start-instances --instance-ids $instanceId --region $Region --profile $Profile | Out-Null
if ($LASTEXITCODE -ne 0) { throw "Could not start the instance." }
& aws ec2 wait instance-running --instance-ids $instanceId --region $Region --profile $Profile
if ($LASTEXITCODE -ne 0) { throw "The instance did not reach the running state." }

$publicDns = (& aws ec2 describe-instances --instance-ids $instanceId --region $Region --profile $Profile `
    --query "Reservations[0].Instances[0].PublicDnsName" --output text).Trim()
$cloudFormationRoleArn = (& aws cloudformation describe-stacks --stack-name $BootstrapStackName --region $Region --profile $Profile `
    --query "Stacks[0].Outputs[?OutputKey=='CloudFormationExecutionRoleArn'].OutputValue" --output text).Trim()

Push-Location $ProjectRoot
try {
    & aws cloudformation deploy `
        --template-file template.yaml `
        --stack-name $StackName `
        --role-arn $cloudFormationRoleArn `
        --capabilities CAPABILITY_NAMED_IAM `
        --no-fail-on-empty-changeset `
        --parameter-overrides `
            "EnvironmentName=$(Get-StackParameter 'EnvironmentName')" `
            "InstanceType=$(Get-StackParameter 'InstanceType')" `
            "VpcId=$(Get-StackParameter 'VpcId')" `
            "SubnetId=$(Get-StackParameter 'SubnetId')" `
            "CloudFrontOriginPrefixListId=$(Get-StackParameter 'CloudFrontOriginPrefixListId')" `
            "DynamoDbTableName=$(Get-StackParameter 'DynamoDbTableName')" `
            "AllowedOrigins=$(Get-StackParameter 'AllowedOrigins')" `
            "RuntimeSecretsParameterName=$(Get-StackParameter 'RuntimeSecretsParameterName')" `
            "SwipeEventsQueueName=$(Get-StackParameter 'SwipeEventsQueueName')" `
            "OriginDomainName=$publicDns" `
        --region $Region `
        --profile $Profile
    if ($LASTEXITCODE -ne 0) { throw "Could not refresh the CloudFront origin after the instance restart." }
} finally {
    Pop-Location
}

$healthUrl = (& aws cloudformation describe-stacks --stack-name $StackName --region $Region --profile $Profile `
    --query "Stacks[0].Outputs[?OutputKey=='HealthUrl'].OutputValue" --output text).Trim()
Write-Host "Restore complete. Health URL: $healthUrl" -ForegroundColor Green
