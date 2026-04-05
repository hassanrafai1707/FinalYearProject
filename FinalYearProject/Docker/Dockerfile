##########################################################
# 1. Build Stage (compile the Spring Boot application)
##########################################################
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Copy Maven wrapper + pom.xml
COPY mvnw ./
COPY .mvn .mvn
COPY pom.xml ./

# Download dependencies (cache layer)
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build the jar
RUN ./mvnw -q -e -DskipTests package

##########################################################
# 2. Runtime Stage (run the app)
##########################################################
FROM eclipse-temurin:21-jre-jammy AS final

ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser

USER appuser

COPY --from=build /app/target/*.jar app.jar

# Create directory for secrets (though Spring Boot reads directly)
# This ensures the user has access to mounted secrets
USER root
RUN mkdir -p /run/secrets && chown appuser:appuser /run/secrets
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]