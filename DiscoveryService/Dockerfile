# Stage 1: Build Stage
FROM maven:3.8.5-openjdk-17-slim as build
ENV JAVA_TOOL_OPTIONS="-XX:-UseContainerSupport"


WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Run Stage (лёгкая JDK версия)
FROM openjdk:17-jdk-slim
ENV JAVA_TOOL_OPTIONS="-XX:-UseContainerSupport"

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8761

CMD ["java", "-jar", "app.jar"]
