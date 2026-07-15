# Flatmate AWS testing deployment

This deployment preserves the existing REST APIs, Spring STOMP/SockJS chat, Redis GEO data, Redis swipe counters, DynamoDB integration, S3 uploads, and JWT security.

The deployed path is:

```text
Frontend -- HTTPS/WSS --> CloudFront --> EC2/Nginx --> Spring Boot + Redis
                                                       |--> DynamoDB
                                                       |--> S3
                                                       `--> SQS
```

CloudFront supplies the stable public HTTPS/WSS hostname. Deployments update containers on the same instance and do not replace the CloudFront distribution.

## Important cost warning

This is not a zero-cost architecture. EC2, its public IPv4 address, EBS, CloudFront, S3, DynamoDB, SQS, and CloudWatch can generate charges. The USD 3 budget described below is an alert, not a spending cap.

The stack deliberately creates no NAT Gateway, load balancer, Elastic IP, RDS, scheduled backup, detailed EC2 monitoring, provisioned Lambda concurrency, or VPC.

| Resource | Why it exists | Cost control | Deletion |
|---|---|---|---|
| One `t3.micro` EC2 instance | Spring WebSocket connections and local Redis require continuous compute | One instance, standard CPU credits, no detailed monitoring | Stack deletion |
| Public IPv4 | Lets CloudFront and the instance reach each other | Exactly one; no Elastic IP | Released when instance stops or terminates |
| 8 GB encrypted gp3 EBS | OS, Docker, and persistent Redis volume | Minimum practical size; no snapshots; delete on termination | Stack deletion |
| CloudFront | Stable HTTPS/WSS URL without a load balancer | PriceClass 100, API caching disabled, no access logs | Stack deletion |
| DynamoDB `FlatmateData` | Existing application data | On-demand is recommended for testing; the stack never creates or deletes it | Preserved by cleanup |
| S3 media bucket | Presigned listing/chat/KYC objects | Private, encrypted, incomplete uploads removed after one day | Cleanup script empties and deletes it |
| S3 deployment bucket | Short-lived release archives | Private, encrypted, three-day lifecycle | Cleanup script removes all versions |
| SQS `swipe-events` and `match-events` | Existing event publisher targets | One-day retention and server-side encryption | Stack deletion |
| CloudWatch Logs | Backend, Redis, and Nginx logs | Three-day retention | Stack deletion |
| CloudFormation | Reproducible infrastructure | No separate CloudFormation charge | Cleanup script |
| GitHub Actions | Test/build/deploy automation | Runs only on `main` and manual dispatch | Disable/delete workflow |
| Internet transfer | Frontend responses and media | Direct S3 presigned uploads avoid routing media through EC2 | Usage stops with cleanup |

## 1. Immediately secure the AWS account

### Verify account information

```text
Open:
AWS Management Console as the root user

Click:
Account name in the top-right corner

Select:
Account

Verify:
Account email, alternate contacts, address, and telephone number

Click:
Update for any incorrect section

Expected result:
The recovery email and telephone number are current and accessible.
```

### Enable root MFA

```text
Open:
AWS Management Console as the root user

Click:
Account name in the top-right corner

Select:
Security credentials

Find:
Multi-factor authentication (MFA)

Click:
Assign MFA device

Enter:
A recognizable device name

Select:
Passkey or Security Key when available; otherwise Authenticator app

Click:
Next

Complete:
The on-screen registration steps

Expected result:
The MFA device is listed as assigned to the root user.
```

Do not create root access keys. Do not use the root account for deployment after the administrative Identity Center user is ready.

### Revoke the previously exposed access key

The retired local `deploy_new_account.ps1` previously contained an AWS access key. Sign in to the account that issued that key and revoke it even if you think it is unused.

```text
Open:
AWS Management Console

Search for:
IAM

Click:
Users

Select:
The user that owned the old access key

Click:
Security credentials

Find:
Access keys

Click:
Actions beside the old key, then Deactivate

After confirming it is not used, click:
Delete

Expected result:
The old access key is absent or marked Inactive.
```

Delete obsolete EC2 key pairs in the old account and securely remove the local `flatmate-prod-key*.pem` files after confirming they are no longer needed.

## 2. Create non-root administrative access with IAM Identity Center

Use the current AWS-recommended temporary-session approach.

```text
Open:
AWS Management Console

Search for:
IAM Identity Center

Click:
Enable

Select:
Enable with AWS Organizations if AWS presents that choice

Click:
Continue

Expected result:
The IAM Identity Center dashboard displays an AWS access portal URL.
```

Create the administrator used for this one-time bootstrap:

```text
Open:
IAM Identity Center

Click:
Users

Click:
Add user

Enter:
Your own business email address and name

Click:
Next, then Add user

Expected result:
AWS emails an invitation to the new user.
```

Create and assign the initial permission set:

```text
Open:
IAM Identity Center

Click:
Permission sets

Click:
Create permission set

Select:
Predefined permission set

Select:
AdministratorAccess

Click:
Next, then Create

Click:
AWS accounts

Select:
The new testing AWS account

Click:
Assign users or groups

Select:
The new Identity Center user

Click:
Next

Select:
AdministratorAccess

Click:
Next, then Submit

Expected result:
The user is assigned AdministratorAccess to the testing account.
```

Accept the invitation, set a unique password, and register MFA. Use this portal identity for the remaining setup.

## 3. Configure AWS CLI SSO on Windows

AWS CLI v2 is already available in the inspected workstation. In PowerShell run:

```powershell
aws configure sso --profile project-testing
```

Enter the values displayed by IAM Identity Center:

```text
SSO session name:
project-testing

SSO start URL:
The AWS access portal URL from IAM Identity Center

SSO region:
The region shown on the IAM Identity Center Settings page

SSO registration scopes:
sso:account:access

AWS account:
Select the testing account

Role:
AdministratorAccess

CLI default client region:
ap-south-1

CLI default output format:
json

CLI profile name:
project-testing
```

Then verify the temporary session:

```powershell
aws sso login --profile project-testing
aws sts get-caller-identity --profile project-testing
```

Success means the final command returns an identity document without an error. Do not send its account ID or ARN to anyone.

AWS SAM CLI and local Docker are not required for this design. CloudFormation deploys the infrastructure and Docker runs only on EC2.

## 4. Configure billing protection before deployment

### Free Tier alerts and CloudWatch billing metrics

```text
Open:
AWS Billing and Cost Management Console

Click:
Billing preferences

Find:
Alert preferences

Click:
Edit

Select:
Receive AWS Free Tier alerts

Select:
Receive CloudWatch Billing Alerts

Enter:
Your billing-alert email address

Click:
Update or Save preferences

Expected result:
Both alert types show as enabled.
```

### USD 3 monthly budget

```text
Open:
AWS Billing and Cost Management Console

Click:
Budgets

Click:
Create budget

Select:
Customize (advanced)

Select:
Cost budget

Click:
Next

Enter budget name:
flatmate-testing-usd-3

Select period:
Monthly

Select budget renewal type:
Recurring budget

Select budgeting method:
Fixed

Enter monthly amount:
3.00 USD

Click:
Next
```

Add email alerts at these thresholds:

| Threshold | Basis |
|---|---|
| 50% | Actual |
| 80% | Actual |
| 100% | Actual |
| 50% | Forecasted |
| 80% | Forecasted |
| 100% | Forecasted |

For each alert choose `% of budgeted amount`, enter the email recipient, then continue to `Next` and `Create budget`.

Budget notifications do not stop services and do not guarantee a maximum charge. Forecast alerts may not work until AWS has enough usage history.

### USD 3 CloudWatch billing alarm

Billing metrics exist only in US East (N. Virginia), even though the application runs in Mumbai.

```text
Open:
AWS Management Console

Change region to:
US East (N. Virginia) us-east-1

Search for:
CloudWatch

Click:
Alarms, then All alarms

Click:
Create alarm

Click:
Select metric

Select:
Billing, then Total Estimated Charge

Select:
EstimatedCharges with Currency USD

Click:
Select metric

Set statistic:
Maximum

Set period:
6 hours

Set threshold type:
Static

Set condition:
Greater than 3 USD

Click:
Next

Create or select:
An SNS topic with your billing email

Click:
Next, enter alarm name flatmate-total-estimated-charge-usd-3, then Create alarm

Expected result:
The alarm appears under All alarms. Confirm the SNS subscription from your email.
```

## 5. Create the encrypted runtime configuration

Generate a JWT signing value locally. Do not share the output:

```powershell
$bytes = New-Object byte[] 48
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
[Convert]::ToBase64String($bytes)
$rng.Dispose()
```

Create the parameter:

```text
Open:
AWS Management Console

Change region to:
Asia Pacific (Mumbai) ap-south-1

Search for:
Systems Manager

Click:
Parameter Store

Click:
Create parameter

Enter name:
/flatmate/testing/runtime-env

Select tier:
Standard

Select type:
SecureString

Select KMS key source:
My current account, using the default aws/ssm key

Paste value:
JWT_SECRET=<paste-the-generated-value>

Click:
Create parameter

Expected result:
/flatmate/testing/runtime-env appears with type SecureString.
```

Optional integrations can be included as additional newline-separated entries:

```text
RAZORPAY_KEY_ID=<value>
RAZORPAY_KEY_SECRET=<value>
DIGILOCKER_CLIENT_ID=<value>
DIGILOCKER_CLIENT_SECRET=<value>
DIGILOCKER_REDIRECT_URI=<value>
DIGILOCKER_BASE_URL=<value>
SUREPASS_TOKEN=<value>
FIREBASE_SERVICE_ACCOUNT_BASE64=<base64-service-account-json>
```

Never add `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, or `AWS_SESSION_TOKEN`. The remote deployment rejects them.

## 6. Perform the first deployment

First sign in again if needed:

```powershell
aws sso login --profile project-testing
```

If `FlatmateData` already exists in this account and region:

```powershell
.\scripts\initial-deploy.ps1 -Profile project-testing
```

If this is definitely the new testing account and `FlatmateData` does not exist:

```powershell
.\scripts\initial-deploy.ps1 -Profile project-testing -CreateDynamoDbTable
```

The explicit switch creates one on-demand, encrypted table with `PK`, `SK`, and `ListingIndex`. It does not enable streams, global tables, provisioned capacity, or point-in-time recovery. The application stack never owns or deletes this table.

The script:

1. Verifies SSO and the SecureString.
2. Checks the DynamoDB table before optionally creating it.
3. Creates the GitHub OIDC and deployment roles.
4. Deploys CloudFormation.
5. Runs tests and builds the JAR.
6. Uploads a three-day deployment artifact.
7. Deploys through Systems Manager without SSH.
8. Checks the local and public health endpoints.
9. Prints the stable base URL and two GitHub role ARNs.

## 7. Configure GitHub repository variables

No AWS access-key secrets are needed.

```text
Open:
GitHub repository rajasekar-cloud/flat_in_mate

Click:
Settings

Click:
Secrets and variables, then Actions

Select:
Variables tab

Click:
New repository variable
```

Create these variables:

| Name | Value |
|---|---|
| `AWS_REGION` | `ap-south-1` |
| `AWS_ROLE_ARN` | `GitHubRoleArn` printed by the initial deployment |
| `CLOUDFORMATION_ROLE_ARN` | `CloudFormationExecutionRoleArn` printed by the initial deployment |
| `CLOUDFORMATION_STACK_NAME` | `flatmate-testing` |
| `DYNAMODB_TABLE_NAME` | `FlatmateData` |
| `RUNTIME_PARAMETER_NAME` | `/flatmate/testing/runtime-env` |
| `ALLOWED_ORIGINS` | Exact comma-separated frontend origins, with no trailing slash |

Example during local frontend testing:

```text
http://localhost:3000,http://localhost:5173
```

Example after the frontend is hosted:

```text
https://testing.example.com,http://localhost:5173
```

Do not create GitHub secrets named `AWS_ACCESS_KEY_ID` or `AWS_SECRET_ACCESS_KEY`.

## 8. Automatic deployment

The deployment branch is `main`, confirmed from the repository's default branch and previous deployment workflow.

```text
1. Make changes on a feature or dev branch.
2. Open a pull request into main.
3. Validate Backend runs tests and CloudFormation linting without deploying.
4. Merge the pull request.
5. Deploy Backend to AWS Testing obtains temporary OIDC credentials.
6. It tests, builds, updates CloudFormation, deploys through SSM, checks health and CORS, and writes the URLs to the job summary.
```

Manual execution:

```text
Open:
GitHub repository

Click:
Actions

Select:
Deploy Backend to AWS Testing

Click:
Run workflow

Select branch:
main

Click:
Run workflow
```

## 9. Get the deployed URLs

```powershell
$BaseUrl = aws cloudformation describe-stacks `
  --stack-name flatmate-testing `
  --region ap-south-1 `
  --profile project-testing `
  --query "Stacks[0].Outputs[?OutputKey=='PublicBaseUrl'].OutputValue" `
  --output text

$HealthUrl = "$BaseUrl/health"
$WebSocketUrl = $BaseUrl.Replace('https://','wss://') + '/ws'

$BaseUrl
$HealthUrl
$WebSocketUrl
```

Do not add a trailing slash to the frontend base URL.

Frontend examples:

```text
VITE_API_BASE_URL=<base-url-without-trailing-slash>
REACT_APP_API_BASE_URL=<base-url-without-trailing-slash>
apiBaseUrl: '<base-url-without-trailing-slash>'
```

## 10. Deployment validation

### Stack, instance, health, and CORS

```powershell
aws cloudformation describe-stacks --stack-name flatmate-testing --region ap-south-1 --profile project-testing --query "Stacks[0].StackStatus"
aws ec2 describe-instances --region ap-south-1 --profile project-testing --filters "Name=tag:Name,Values=flatmate-testing" --query "Reservations[0].Instances[0].State.Name"
curl.exe -i "$BaseUrl/health"
curl.exe -i -X OPTIONS "$BaseUrl/health" -H "Origin: http://localhost:5173" -H "Access-Control-Request-Method: GET" -H "Access-Control-Request-Headers: Authorization,Content-Type"
```

Expected results: stack `CREATE_COMPLETE` or `UPDATE_COMPLETE`, instance `running`, health HTTP 200, and CORS response containing the requested allowed origin.

### Public endpoint and invalid-JWT rejection

```powershell
curl.exe -i -X POST "$BaseUrl/auth/otp/send" -H "Content-Type: application/json" -d '{"phone":"deployment-test"}'
curl.exe -i "$BaseUrl/profiles/deployment-test" -H "Authorization: Bearer invalid-token"
```

Expected results: OTP endpoint HTTP 200; protected endpoint HTTP 401 or 403 for the invalid token.

### Safe DynamoDB write, read, and authenticated endpoint

The current development authentication implementation uses OTP `123456`. This creates one clearly named testing user in DynamoDB.

```powershell
$TestUser = "deployment-test-$([DateTimeOffset]::UtcNow.ToUnixTimeSeconds())"
$AuthResponse = curl.exe -sS -X POST "$BaseUrl/auth/otp/verify" `
  -H "Content-Type: application/json" `
  -d "{`"phone`":`"$TestUser`",`"otp`":`"123456`"}" | ConvertFrom-Json

$Token = $AuthResponse.accessToken
curl.exe -i "$BaseUrl/profiles/$TestUser" -H "Authorization: Bearer $Token"
```

The OTP verification performs a DynamoDB write and the profile request performs a DynamoDB read. Do not use a real phone number.

### DynamoDB configuration report

```powershell
aws dynamodb describe-table --table-name FlatmateData --region ap-south-1 --profile project-testing --query "Table.{BillingMode:BillingModeSummary.BillingMode,Status:TableStatus,Streams:StreamSpecification,GlobalSecondaryIndexes:GlobalSecondaryIndexes[].IndexName}"
aws dynamodb describe-continuous-backups --table-name FlatmateData --region ap-south-1 --profile project-testing
```

### Logs

```powershell
aws logs tail /flatmate/testing/containers --since 10m --follow --region ap-south-1 --profile project-testing
```

Console path:

```text
Open:
AWS Management Console

Search for:
CloudWatch

Click:
Logs, then Log groups

Select:
/flatmate/testing/containers

Expected result:
backend, nginx, and redis streams are visible.
```

### CI/CD and stable URL validation

1. Record `$BaseUrl`.
2. Merge a harmless test change into `main`.
3. Open GitHub `Actions` > `Deploy Backend to AWS Testing` > latest run.
4. Confirm every step is green and inspect the job summary.
5. Retrieve `$BaseUrl` again and confirm it is identical.

## 11. Rollback

The instance keeps the three most recent releases. To switch to the previous healthy release:

```powershell
.\scripts\rollback.ps1 -Profile project-testing
```

The script uses Systems Manager, restarts the previous containers, and restores the current release if rollback health validation fails.

## 12. Emergency stop and restore

Stop application execution immediately:

```powershell
.\scripts\emergency-stop.ps1 -Profile project-testing
```

This stops EC2 compute charges but does not stop EBS, CloudFront, S3, DynamoDB, logs, or other storage/request charges.

Restore the instance and refresh CloudFront after its public hostname changes:

```powershell
.\scripts\restore-testing-stack.ps1 -Profile project-testing
```

## 13. Full cleanup

Delete testing compute and stack-owned data while preserving DynamoDB:

```powershell
.\scripts\delete-testing-stack.ps1 -Profile project-testing
```

Preview without deleting:

```powershell
.\scripts\delete-testing-stack.ps1 -Profile project-testing -WhatIf
```

Also delete the GitHub OIDC/deployment bootstrap stack only when this repository will never deploy again:

```powershell
.\scripts\delete-testing-stack.ps1 -Profile project-testing -DeleteBootstrap
```

The cleanup never deletes `FlatmateData`, DynamoDB records, the GitHub repository, or unrelated AWS resources. It does delete stack-owned testing media in S3 and the Redis data stored on the instance volume.
