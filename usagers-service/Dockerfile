FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies first (caches layers for faster rebuilds)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and package the JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy only the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose default Spring Boot port + gRPC port
EXPOSE 8083 9091

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
