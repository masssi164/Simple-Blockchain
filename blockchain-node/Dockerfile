# ------------------------------------------------
# 1) Build Stage: Gradle builds the Spring Boot JAR
# ------------------------------------------------
FROM gradle:8.7.0-jdk21 AS build
WORKDIR /workspace

# Optional certificate to trust during dependency downloads
ARG BUILD_CA_CERT
ENV BUILD_CA_CERT=${BUILD_CA_CERT}

# Full project is required for a multi-module build
COPY . .

# If a custom cert path is provided and exists, install it
RUN if [ -n "$BUILD_CA_CERT" ] && [ -f "$BUILD_CA_CERT" ]; then \
      cp "$BUILD_CA_CERT" /usr/local/share/ca-certificates/custom.crt && \
      update-ca-certificates; \
    fi

# --- Zertifikat ebenfalls in den JVM-Truststore legen --------------
RUN if [ -n "$BUILD_CA_CERT" ] && [ -f "$BUILD_CA_CERT" ]; then \
      keytool -importcert \
              -alias zscaler \
              -file "$BUILD_CA_CERT" \
              -keystore "$JAVA_HOME/lib/security/cacerts" \
              -storepass changeit \
              -noprompt \
              -trustcacerts && \
      # Symlink, damit Linux-Store und JVM-Store identisch bleiben
      ln -sf /etc/ssl/certs/java/cacerts "$JAVA_HOME/lib/security/cacerts"; \
    fi

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
