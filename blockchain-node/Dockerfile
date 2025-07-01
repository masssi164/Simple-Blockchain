# ------------------------------------------------
# 1) Build Stage: Gradle builds the Spring Boot JAR
# ------------------------------------------------
FROM gradle:8.7.0-jdk21 AS build
WORKDIR /workspace

# Full project is required for a multi-module build
COPY . .

# Build only the node module
RUN gradle :blockchain-node:bootJar --no-daemon

# ------------------------------------------------
# 2) Runtime Stage: slim JRE with the built JAR
# ------------------------------------------------
FROM eclipse-temurin:21-jre

# Build-time port is passed from docker-compose
ARG SERVER_PORT=3333
ENV SERVER_PORT=${SERVER_PORT}
WORKDIR /app

# Copy the jar produced in the build stage
COPY --from=build /workspace/blockchain-node/build/libs/*.jar app.jar

EXPOSE ${SERVER_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]
