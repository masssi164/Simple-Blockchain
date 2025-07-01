FROM eclipse-temurin:21-jre
ARG SERVER_PORT=3333
ENV SERVER_PORT=${SERVER_PORT}
WORKDIR /app
COPY *.jar app.jar
# Expose the port defined by SERVER_PORT (defaults to 3333)
EXPOSE ${SERVER_PORT}
ENTRYPOINT ["java", "-jar", "app.jar"]
