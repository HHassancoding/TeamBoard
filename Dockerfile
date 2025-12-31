FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/teamboard-0.1.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 8080
