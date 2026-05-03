# ---- Run Stage ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the pre-built jar from the target folder (built by GitHub Actions)
COPY target/*.jar app.jar

EXPOSE 8081

# Run the application with optimized memory settings for t3.micro (1GB RAM)
# Reduced heap to 384m to leave room for OS, Redis, and Nginx
ENTRYPOINT ["java", "-Xmx384m", "-jar", "app.jar"]
