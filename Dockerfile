
# Stage 1: Build
FROM eclipse-temurin:27-jdk AS build

WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven \
    && rm -rf /var/lib/apt/lists/*

# Copy Maven configuration
COPY pom.xml .
COPY src ./src

# Build application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:27-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]