# Stage 1: Node dependencies
FROM node:20.11.0 AS node-deps
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci

# Stage 2: Build the application
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app

# Copy gradle files
COPY gradlew .
COPY gradle gradle
RUN chmod +x gradlew

# Copy source configuration
COPY build.gradle settings.gradle ./

# Copy all source
COPY src src
COPY frontend frontend

# Copy node_modules from node-deps
COPY --from=node-deps /app/frontend/node_modules frontend/node_modules

# Build application
RUN ./gradlew build -x test --no-watch-fs --stacktrace

# Stage 3: Create the runtime image
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
