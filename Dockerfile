# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]