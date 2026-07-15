[CmdletBinding(SupportsShouldProcess)]
param(
    [string]$Profile = "project-testing",
    [string]$Region = "ap-south-1",
    [string]$StackName = "flatmate-testing"
)

$ErrorActionPreference = "Stop"
$instanceId = (& aws cloudformation describe-stacks `
    --stack-name $StackName --region $Region --profile $Profile `
    --query "Stacks[0].Outputs[?OutputKey=='InstanceId'].OutputValue" --output text).Trim()
if ($LASTEXITCODE -ne 0 -or -not $instanceId) { throw "Could not find the testing instance." }

if ($PSCmdlet.ShouldProcess($instanceId, "Stop the EC2 instance and prevent application requests")) {
    & aws ec2 stop-instances --instance-ids $instanceId --region $Region --profile $Profile | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "Could not stop the instance." }
    & aws ec2 wait instance-stopped --instance-ids $instanceId --region $Region --profile $Profile
    if ($LASTEXITCODE -ne 0) { throw "The instance did not reach the stopped state." }
    Write-Host "Emergency stop complete. EC2 compute billing is stopped; EBS, S3, CloudFront, logs, DynamoDB, and stored data can still incur charges." -ForegroundColor Yellow
    Write-Host "Restore with: .\scripts\restore-testing-stack.ps1 -Profile $Profile -Region $Region -StackName $StackName"
}
