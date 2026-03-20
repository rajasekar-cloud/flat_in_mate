#!/bin/bash

# Configuration
EC2_IP="13.235.241.112"
KEY_FILE="flatmate-prod-key.pem"
USER="ec2-user"
PROJECT_DIR="flat_in_mate"

echo "================================================="
echo "   Deploying Flatmate App to EC2 ($EC2_IP)       "
echo "================================================="

# 1. Check Key
if [ ! -f "$KEY_FILE" ]; then
    echo "Error: $KEY_FILE not found in the current directory!"
    exit 1
fi

# Set strict permissions on the key (required by SSH)
chmod 400 $KEY_FILE

echo "[+] Waiting for EC2 to fully boot and accept SSH connections..."
# Give it a few seconds just in case it was literally just created
sleep 15

# 2. Setup Server Infrastructure (Install Docker & Git)
echo "[+] Configuring EC2 instance (Installing Docker, Docker Compose, Java)..."
ssh -i $KEY_FILE -o StrictHostKeyChecking=no $USER@$EC2_IP << 'EOF'
    sudo yum update -y
    sudo yum install -y docker git
    sudo systemctl enable docker
    sudo systemctl start docker
    sudo usermod -a -G docker ec2-user

    # Install Docker Compose
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose
EOF

# 3. Synchronize Code to EC2 Server
echo "[+] Copying application code to EC2 (Excluding target/ and .git/)..."
# We use rsync or scp to sync it over. Let's create a tarball first to make it fast via SSH.
tar --exclude='target' --exclude='.git' --exclude='.idea' --exclude='.vscode' --exclude='node_modules' -czf deploy_payload.tar.gz .

scp -i $KEY_FILE -o StrictHostKeyChecking=no deploy_payload.tar.gz $USER@$EC2_IP:~

echo "[+] Building and starting the application on EC2..."
ssh -i $KEY_FILE $USER@$EC2_IP << 'EOF'
    # Create project directory
    mkdir -p flat_in_mate
    mv deploy_payload.tar.gz flat_in_mate/
    cd flat_in_mate
    tar -xzf deploy_payload.tar.gz
    rm deploy_payload.tar.gz

    # Use the docker-compose file to build and run the services
    # (assuming docker-compose.yml has both the app and redis)
    
    # In case the user session didn't pick up the 'docker' group properly yet,
    # we run the compose command using 'sg docker -c' to ensure it executes with docker privileges
    sg docker -c "docker-compose up -d --build"
    
    echo "Containers currently running:"
    sg docker -c "docker ps"
EOF

# Cleanup local tarball
rm deploy_payload.tar.gz

echo "================================================="
echo "Deployment Complete!"
echo "Your API should be live in a minute at: http://$EC2_IP:8081"
echo "To check logs, SSH into the machine:"
echo "ssh -i $KEY_FILE ec2-user@$EC2_IP"
echo "================================================="
