# Stage 1: Build using Maven and Eclipse Temurin JDK 17
FROM maven:3.8.5-eclipse-temurin-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run using Eclipse Temurin JRE 17 (Lightweight)
FROM eclipse-temurin:17-jre-alpine
COPY --from=build /target/banking-app-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
