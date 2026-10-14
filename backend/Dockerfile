# ===================================================================
# Stage 1: Build the Spring Boot application using Maven
# ===================================================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Cache Maven dependencies by copying pom.xml first
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy application source code and package executable JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ===================================================================
# Stage 2: Create a secure, lightweight runtime container
# ===================================================================
FROM eclipse-temurin:21-jre-alpine

# Security: Run as non-root user
RUN addgroup -S finledger && adduser -S finledger -G finledger

WORKDIR /app

# Copy the packaged jar from builder stage
COPY --from=builder /build/target/finledger-backend-1.0.0.jar /app/finledger.jar

RUN chown -R finledger:finledger /app

USER finledger

# Render and local defaults
ENV PORT=8080
EXPOSE 8080

# Run with tuned JVM options for containers
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/finledger.jar"]
