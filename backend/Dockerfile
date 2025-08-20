# Dockerfile for Spring Boot Backend
# 1. Use an official OpenJDK runtime as a parent image
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml /app
RUN mvn dependency:go-offline
COPY src /app/src
RUN mvn clean package -DskipTests "-Dspring.profiles.active=docker"

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY wait-for-it.sh /wait-for-it.sh
ENTRYPOINT ["sh", "-c", "/wait-for-it.sh db:5432 -- java -jar app.jar --spring.profiles.active=docker"]

