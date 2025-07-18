# syntax=docker/dockerfile:1.4
# ------------------------------------------------
# 1) Build Stage: Gradle builds the Spring Boot JAR
# ------------------------------------------------
FROM gradle:8.7.0-jdk21 AS build
WORKDIR /workspace

# Full project is required for a multi-module build
COPY . .

# Optionally trust a CA certificate provided via secret
RUN --mount=type=secret,id=zscaler_cert,required=false \
    if [ -s /run/secrets/zscaler_cert ]; then \
        cp /run/secrets/zscaler_cert /usr/local/share/ca-certificates/zscaler.crt && \
        update-ca-certificates && \
        if keytool -importcert -alias zscaler -file /run/secrets/zscaler_cert \
                -cacerts -storepass changeit -trustcacerts -noprompt; then \
            echo "→ Zertifikat in Standard-cacerts importiert"; \
        else \
            echo "→ keytool -cacerts fehlgeschlagen, erstelle beschreibbaren Truststore"; \
            mkdir -p /etc/ssl/certs/java && \
            cp "$JAVA_HOME/lib/security/cacerts" /etc/ssl/certs/java/cacerts && \
            ln -sf /etc/ssl/certs/java/cacerts "$JAVA_HOME/lib/security/cacerts" && \
            keytool -importcert -alias zscaler -file /run/secrets/zscaler_cert \
                -keystore /etc/ssl/certs/java/cacerts -storepass changeit -trustcacerts -noprompt; \
        fi; \
    else \
        echo "→ Kein Zertifikat gefunden – Import übersprungen"; \
    fi

# Build only the node module
RUN gradle :blockchain-node:bootJar --no-daemon

# ------------------------------------------------
# 2) Runtime Stage: slim JRE with the built JAR
# ------------------------------------------------
FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Build-time port is passed from docker-compose
ARG SERVER_PORT=3333
ENV SERVER_PORT=${SERVER_PORT}
WORKDIR /app

# Copy the jar produced in the build stage
COPY --from=build /workspace/blockchain-node/build/libs/*.jar app.jar

EXPOSE ${SERVER_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]
