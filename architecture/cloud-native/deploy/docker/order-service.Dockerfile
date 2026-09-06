# Multi-stage build — no Docker daemon needed for build, Jib is the preferred alternative (see README)
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace
COPY . .
RUN mvn -q package -pl order-bootstrap -am -f ../cloud-native 2>/dev/null || true

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# Copy the fat jar built by: mvn package -f architecture/cloud-native/pom.xml
COPY order-bootstrap/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]