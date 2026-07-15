[CmdletBinding()]
param(
    [string]$Profile = "project-testing",
    [string]$Region = "ap-south-1",
    [string]$StackName = "flatmate-testing"
)

$ErrorActionPreference = "Stop"
$instanceId = (& aws cloudformation describe-stacks --stack-name $StackName --region $Region --profile $Profile `
    --query "Stacks[0].Outputs[?OutputKey=='InstanceId'].OutputValue" --output text).Trim()
if ($LASTEXITCODE -ne 0 -or -not $instanceId) { throw "Could not find the testing instance." }

$parameters = Join-Path $env:TEMP "flatmate-rollback-parameters.json"
$json = @{ commands = @("sudo bash /opt/flatmate/current/scripts/remote-rollback.sh") } | ConvertTo-Json -Depth 3
[System.IO.File]::WriteAllText($parameters, $json, [System.Text.UTF8Encoding]::new($false))
try {
    $commandId = (& aws ssm send-command --instance-ids $instanceId --document-name AWS-RunShellScript `
        --comment "Rollback Flatmate testing backend" --parameters "file://$parameters" `
        --query "Command.CommandId" --output text --region $Region --profile $Profile).Trim()
    if ($LASTEXITCODE -ne 0) { throw "Could not start rollback." }
    & aws ssm wait command-executed --command-id $commandId --instance-id $instanceId --region $Region --profile $Profile
    if ($LASTEXITCODE -ne 0) { throw "Rollback command failed." }
    & aws ssm get-command-invocation --command-id $commandId --instance-id $instanceId `
        --query "{Status:Status,Output:StandardOutputContent,Error:StandardErrorContent}" --region $Region --profile $Profile
} finally {
    Remove-Item -LiteralPath $parameters -Force -ErrorAction SilentlyContinue
}
