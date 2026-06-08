# Build context: backend/ (o build multi-modulo precisa do parent pom + shared).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -B -pl service-agenda -am clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/service-agenda/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
