# Payment service container — built from payment-service fat jar
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY payment-service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]