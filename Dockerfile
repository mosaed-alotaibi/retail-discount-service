# ========================================
# Multi-Stage Dockerfile for Spring Boot 3.5.6
# Java 21 | Maven Build | Production-Ready
# ========================================

# ========================================
# Stage 1: Build Stage
# ========================================
FROM eclipse-temurin:21-jdk-alpine AS build

# Set working directory
WORKDIR /workspace/app

# Copy Maven wrapper and pom.xml first (for layer caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached layer if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application (skip tests for faster builds in Docker)
# Tests should run in CI/CD pipeline
RUN ./mvnw clean package -DskipTests

# Extract JAR layers for better Docker layer caching
RUN mkdir -p target/dependency && \
    (cd target/dependency; jar -xf ../retail-discount-service.jar)

# ========================================
# Stage 2: Runtime Stage
# ========================================
FROM eclipse-temurin:21-jre-alpine

# Add metadata labels
LABEL maintainer="your.email@example.com"
LABEL application="retail-discount-service"
LABEL version="1.0.0"
LABEL description="REST API for retail discount calculations"

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Set working directory
WORKDIR /app

# Copy the extracted application from build stage
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

# Change ownership to non-root user
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:InitialRAMPercentage=50.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -Djava.security.egd=file:/dev/./urandom"

# Run the application with optimized JVM settings
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -cp app:app/lib/* com.retail.discount.RetailDiscountApplication"]