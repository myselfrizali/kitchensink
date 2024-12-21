# Stage 1: Build the application
FROM maven:3.9.9-amazoncorretto-21 AS builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Create a minimal runtime image
FROM openjdk:21-jdk-slim

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

#ENV DB_URL jdbc:mysql://host.docker.internal:3306/chauffeur
#ENV DB_USERNAME root
#ENV DB_PASSWORD root

ENTRYPOINT ["java","-jar","app.jar"]
