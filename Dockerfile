# ---- Run Stage ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the pre-built jar from the target folder (built by GitHub Actions)
COPY target/*.jar app.jar

EXPOSE 8081

# Run the application with optimized memory settings
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
