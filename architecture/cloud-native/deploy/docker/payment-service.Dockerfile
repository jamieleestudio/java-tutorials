# Payment service container — built from payment-bootstrap fat jar
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY payment-bootstrap/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]