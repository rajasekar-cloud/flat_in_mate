# The build/test stage runs in CI. This first stage isolates the built artifact
# so the runtime image contains only Java and the application JAR.
FROM amazoncorretto:17-alpine AS artifact
WORKDIR /artifact
COPY target/*.jar app.jar

FROM amazoncorretto:17-alpine AS runtime
WORKDIR /app
COPY --from=artifact /artifact/app.jar /app/app.jar

RUN addgroup -S flatmate && adduser -S flatmate -G flatmate
USER flatmate

EXPOSE 8081

ENTRYPOINT ["java", "-Xms128m", "-Xmx384m", "-XX:+UseSerialGC", "-jar", "/app/app.jar"]
