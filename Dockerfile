# --- Build stage: compile and package the Spring Boot executable jar ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew clean bootJar -x test --no-daemon

# --- Run stage: small JRE image that runs the jar ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/app.jar app.jar
# The app reads the PORT env var (server.port=${PORT:8080}); Railway injects it.
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
