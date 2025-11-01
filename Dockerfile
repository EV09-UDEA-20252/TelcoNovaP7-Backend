FROM eclipse-temurin:21-jre-alpine

WORKDIR /APP

COPY target/TelcoNovaP7-Backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]

