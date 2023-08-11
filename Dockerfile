FROM maven:3.9.0-eclipse-temurin-19 as build
ADD . .
RUN mvn clean package
FROM eclipse-temurin:19.0.2_7-jre
COPY --from=build target/*.jar api.jar
COPY --from=build src/main/resources /data
CMD ["java", "-jar", "api.jar"]
EXPOSE 8080