# Stage 1: Build the application
FROM eclipse-temurin:21-jdk AS builder
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

# Build application
RUN ./gradlew build -x test --no-watch-fs --stacktrace

# Stage 2: Create the runtime image
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
