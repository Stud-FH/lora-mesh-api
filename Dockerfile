FROM maven:3.9.0-eclipse-temurin-19 as build
ADD . .
RUN mvn clean package
FROM eclipse-temurin:19.0.2_7-jre
COPY --from=build target/*.jar api.jar
CMD ["java", "-jar", "api.jar"]
EXPOSE 8080