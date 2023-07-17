FROM maven:3.9.0-eclipse-temurin-19 AS build
WORKDIR /source
COPY pom.xml .
COPY . ./
RUN mvn package

FROM eclipse-temurin:19.0.2_7-jre
WORKDIR /app
COPY --from=build /source/target/*.jar app.jar
ENTRYPOINT ["cat"]
EXPOSE 8080