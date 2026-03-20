# Flat-Sharing App (Modular Monolith)

This is a unified backend application for flat-sharing, organized into modules (packages) and running on port **8080**.

## Instructions

### 1. Run via VS Code (Recommended)
1. Open the project in VS Code.
2. Open `src/main/java/com/flatmate/app/FlatmateApplication.java`.
3. Click the **"Run"** button above the `main` method.
*Note: Make sure you have the "Java Extension Pack" installed.*

### 2. Run via Command Line (using Maven Wrapper)
If you don't have Maven installed, use the included wrapper:
```powershell
.\mvnw spring-boot:run
```

---

### Prerequisites
- **Docker**: For running DynamoDB Local and Redis.
- **Port 8000**: DynamoDB Local.
- **Port 8001**: DynamoDB Admin (Check your data at `http://localhost:8001`).
- **Port 6379**: Redis.

### Setup
1. Start the services:
   ```powershell
   docker-compose up -d
   ```
2. Build and run:
   ```powershell
   .\mvnw clean compile
   .\mvnw spring-boot:run
   ```
