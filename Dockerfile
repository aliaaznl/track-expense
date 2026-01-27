# 1. Build the app using Maven
FROM maven:3.8.5-openjdk-17 AS build
COPY src/main/java .
RUN mvn clean package -DskipTests

# 2. Run the app using a lightweight Java image
FROM openjdk:17-jdk-slim
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]