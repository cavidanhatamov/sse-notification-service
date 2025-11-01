# Multi-stage Dockerfile for Spring Boot SSE Application

# Stage 1: Build the application
FROM openjdk:25-jdk-slim AS builder

WORKDIR /app

# Copy Gradle wrapper and configuration files
COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./

# Make gradlew executable
RUN chmod +x gradlew

# Copy source code
COPY src/ src/

# Build the application (skip tests for faster builds)
RUN ./gradlew clean bootJar -x test

# Stage 2: Create the runtime image
FROM openjdk:25-jre-slim

# Install curl for health checks
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# Create app directory and user
RUN groupadd -r spring && useradd -r -g spring spring
RUN mkdir -p /app/logs && chown -R spring:spring /app

WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Set ownership
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring

# Set JVM options optimized for containerized environments
ENV JAVA_OPTS="-Xms2g -Xmx4g \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -Dio.netty.eventLoopThreads=8 \
    -Dreactor.netty.ioWorkerCount=8 \
    -Djava.security.egd=file:/dev/./urandom"

# Expose application port
EXPOSE 9090

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:9090/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Labels for documentation
LABEL maintainer="SSE Notification System"
LABEL description="Spring Boot application with reactive SSE, Kafka, and MongoDB"
LABEL version="1.0.0"
