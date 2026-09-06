# Product service container — built from product-bootstrap fat jar
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY product-bootstrap/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]