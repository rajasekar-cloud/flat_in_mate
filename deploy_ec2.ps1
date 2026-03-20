$EC2_IP = "13.235.241.112"
$KEY_FILE = "flatmate-prod-key-ascii.pem"
$USER = "ec2-user"

Write-Host "=================================================" -ForegroundColor Cyan
Write-Host "   Deploying Flatmate App to EC2 ($EC2_IP)       " -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan

# 1. Check Key
if (-not (Test-Path $KEY_FILE)) {
    Write-Host "Error: $KEY_FILE not found in the current directory!" -ForegroundColor Red
    exit 1
}

Write-Host "[+] Setting permissions on $KEY_FILE (simulated for Windows ssh)" -ForegroundColor Yellow
icacls $KEY_FILE /inheritance:r
icacls $KEY_FILE /grant:r "$($env:USERNAME):R"

Write-Host "[+] Waiting for EC2 to fully boot and accept SSH connections (15s)..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# 2. Setup Server Infrastructure (Install Docker & Git)
Write-Host "[+] Configuring EC2 instance (Installing Docker, Docker Compose)..." -ForegroundColor Yellow
$setupCommand = @"
    sudo yum update -y
    sudo yum install -y docker git
    sudo systemctl enable docker
    sudo systemctl start docker
    sudo usermod -a -G docker ec2-user

    sudo curl -L `"https://github.com/docker/compose/releases/latest/download/docker-compose-`$(uname -s)-`$(uname -m)`" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    if [ ! -f /usr/bin/docker-compose ]; then
        sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose
    fi
"@

ssh -i $KEY_FILE -o StrictHostKeyChecking=no "$USER@$EC2_IP" $setupCommand

# 3. Synchronize Code to EC2 Server
Write-Host "[+] Packaging application code..." -ForegroundColor Yellow
# Rather than tar, we will use Windows native compression
if (Test-Path deploy_payload.zip) { Remove-Item deploy_payload.zip }

# Zip everything except node_modules, target, .git, etc.
$excludeList = @(".git", "target", ".idea", ".vscode", "deploy_payload.zip", "*.pem")
Get-ChildItem -Exclude $excludeList | Compress-Archive -DestinationPath deploy_payload.zip -Force

Write-Host "[+] Uploading code to EC2..." -ForegroundColor Yellow
scp -i $KEY_FILE -o StrictHostKeyChecking=no deploy_payload.zip "$USER@${EC2_IP}:~/"

Write-Host "[+] Building and starting the application on EC2..." -ForegroundColor Yellow
$deployCommand = @"
    mkdir -p flat_in_mate
    mv deploy_payload.zip flat_in_mate/
    cd flat_in_mate
    unzip -o deploy_payload.zip
    rm deploy_payload.zip

    # Apply group changes dynamically to use docker
    # Build and start the containers
    sg docker -c `"docker-compose up -d --build`"
    
    echo `"Containers currently running:`"
    sg docker -c `"docker ps`"
"@

ssh -i $KEY_FILE "$USER@$EC2_IP" $deployCommand

# Cleanup local zip
Remove-Item deploy_payload.zip

Write-Host "=================================================" -ForegroundColor Green
Write-Host "Deployment Completed Successfully!" -ForegroundColor Green
Write-Host "Your API should be live in a minute at: http://$EC2_IP:8081" -ForegroundColor Green
Write-Host "To check logs, SSH into the machine: ssh -i $KEY_FILE ec2-user@$EC2_IP" -ForegroundColor Green
Write-Host "=================================================" -ForegroundColor Green
