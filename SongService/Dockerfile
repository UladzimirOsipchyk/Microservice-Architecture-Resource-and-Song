# Stage 1: Build Stage
FROM maven:3.8.5-openjdk-17-slim as build
#PLEASE READ description !!!
#tried openjdk:17-alpine and openjdk:17-jdk-alpine

#I have M1 MacBook and have an issue with manifest for linux/arm64/v8 for my chip.
#There is no alpine version for it, so I used slim one
#Error response from daemon: no matching manifest for linux/arm64/v8 in the manifest list entries
#Failed to deploy '<unknown> Dockerfile: ResourceService/Dockerfile': Can't retrieve image ID from build stream


WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Run Stage (лёгкая JDK версия)
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

CMD ["java", "-jar", "app.jar"]
