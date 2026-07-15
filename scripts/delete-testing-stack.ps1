[CmdletBinding(SupportsShouldProcess)]
param(
    [string]$Profile = "project-testing",
    [string]$Region = "ap-south-1",
    [string]$StackName = "flatmate-testing",
    [string]$BootstrapStackName = "flatmate-github-oidc",
    [switch]$DeleteBootstrap
)

$ErrorActionPreference = "Stop"

function Invoke-Aws {
    param([Parameter(Mandatory)][string[]]$Arguments)
    $result = & aws @Arguments
    if ($LASTEXITCODE -ne 0) { throw "AWS CLI command failed: aws $($Arguments -join ' ')" }
    return $result
}

function Get-OutputValue {
    param([string]$Key)
    return (Invoke-Aws -Arguments @(
        "cloudformation", "describe-stacks", "--stack-name", $StackName,
        "--query", "Stacks[0].Outputs[?OutputKey=='$Key'].OutputValue", "--output", "text",
        "--region", $Region, "--profile", $Profile
    )).Trim()
}

function Remove-VersionedBucketContents {
    param([string]$BucketName)
    while ($true) {
        $listingJson = Invoke-Aws -Arguments @(
            "s3api", "list-object-versions", "--bucket", $BucketName, "--max-keys", "1000",
            "--region", $Region, "--profile", $Profile, "--output", "json"
        )
        $listing = ($listingJson -join "`n") | ConvertFrom-Json

        $objects = @()
        if ($listing.Versions) {
            $objects += $listing.Versions | ForEach-Object { @{ Key = $_.Key; VersionId = $_.VersionId } }
        }
        if ($listing.DeleteMarkers) {
            $objects += $listing.DeleteMarkers | ForEach-Object { @{ Key = $_.Key; VersionId = $_.VersionId } }
        }
        if ($objects.Count -eq 0) { break }

        $deleteFile = Join-Path $env:TEMP "flatmate-delete-objects.json"
        $json = @{ Objects = $objects; Quiet = $true } | ConvertTo-Json -Depth 5
        [System.IO.File]::WriteAllText($deleteFile, $json, [System.Text.UTF8Encoding]::new($false))
        try {
            Invoke-Aws -Arguments @(
                "s3api", "delete-objects", "--bucket", $BucketName, "--delete", "file://$deleteFile",
                "--region", $Region, "--profile", $Profile
            ) | Out-Null
        } finally {
            Remove-Item -LiteralPath $deleteFile -Force -ErrorAction SilentlyContinue
        }
    }
}

function Stop-MultipartUploads {
    param([string]$BucketName)
    while ($true) {
        $uploadJson = Invoke-Aws -Arguments @(
            "s3api", "list-multipart-uploads", "--bucket", $BucketName, "--max-uploads", "1000",
            "--region", $Region, "--profile", $Profile, "--output", "json"
        )
        $uploadListing = ($uploadJson -join "`n") | ConvertFrom-Json
        if (-not $uploadListing.Uploads -or $uploadListing.Uploads.Count -eq 0) { break }

        foreach ($upload in $uploadListing.Uploads) {
            Invoke-Aws -Arguments @(
                "s3api", "abort-multipart-upload", "--bucket", $BucketName,
                "--key", $upload.Key, "--upload-id", $upload.UploadId,
                "--region", $Region, "--profile", $Profile
            ) | Out-Null
        }
    }
}

Invoke-Aws -Arguments @("sts", "get-caller-identity", "--profile", $Profile, "--region", $Region) | Out-Null

$deploymentBucket = Get-OutputValue "DeploymentBucketName"
$mediaBucket = Get-OutputValue "MediaBucketName"

Write-Warning "This deletes the testing compute, CloudFront URL, Redis data, testing S3 media, SQS queue, and logs."
Write-Host "It does NOT delete DynamoDB table FlatmateData or its data." -ForegroundColor Green

if (-not $PSCmdlet.ShouldProcess($StackName, "Empty stack-owned S3 buckets and delete the CloudFormation stack")) {
    return
}

if ($deploymentBucket) {
    Stop-MultipartUploads $deploymentBucket
    Remove-VersionedBucketContents $deploymentBucket
}
if ($mediaBucket) {
    Stop-MultipartUploads $mediaBucket
    Invoke-Aws -Arguments @("s3", "rm", "s3://$mediaBucket", "--recursive", "--only-show-errors", "--region", $Region, "--profile", $Profile) | Out-Null
}

$cloudFormationRoleArn = (Invoke-Aws -Arguments @(
    "cloudformation", "describe-stacks", "--stack-name", $BootstrapStackName,
    "--query", "Stacks[0].Outputs[?OutputKey=='CloudFormationExecutionRoleArn'].OutputValue", "--output", "text",
    "--region", $Region, "--profile", $Profile
)).Trim()

Invoke-Aws -Arguments @(
    "cloudformation", "delete-stack", "--stack-name", $StackName, "--role-arn", $cloudFormationRoleArn,
    "--region", $Region, "--profile", $Profile
) | Out-Null
Invoke-Aws -Arguments @("cloudformation", "wait", "stack-delete-complete", "--stack-name", $StackName, "--region", $Region, "--profile", $Profile) | Out-Null

if ($DeleteBootstrap) {
    Invoke-Aws -Arguments @("cloudformation", "delete-stack", "--stack-name", $BootstrapStackName, "--region", $Region, "--profile", $Profile) | Out-Null
    Invoke-Aws -Arguments @("cloudformation", "wait", "stack-delete-complete", "--stack-name", $BootstrapStackName, "--region", $Region, "--profile", $Profile) | Out-Null
}

Write-Host "Testing stack deleted. DynamoDB was preserved." -ForegroundColor Green
