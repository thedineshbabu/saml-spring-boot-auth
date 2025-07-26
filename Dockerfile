# Multi-stage build for Spring Boot SAML Application
# 
# This Dockerfile creates a production-ready container for the
# SAML Spring Boot authentication application.

# Stage 1: Build the application
FROM maven:3.9.5-openjdk-17 AS build

# Set working directory
WORKDIR /app

# Copy Maven configuration files
COPY pom.xml .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM openjdk:17-jre-slim

# Set metadata
LABEL maintainer="SAML Spring Boot Application Team"
LABEL version="1.0.0"
LABEL description="SAML Spring Boot Authentication Application"

# Create application user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/saml-spring-boot-auth-1.0.0.jar app.jar

# Create directories for logs and certificates
RUN mkdir -p /app/logs /app/credentials && \
    chown -R appuser:appuser /app

# Switch to application user
USER appuser

# Expose application port
EXPOSE 8080

# Set JVM options for production
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 