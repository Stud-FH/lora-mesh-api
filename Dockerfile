FROM maven:3.9.0-eclipse-temurin-19 AS build
WORKDIR /source
COPY pom.xml .
RUN mvn compile assembly:single --fail-never
COPY . ./
RUN ls
RUN mvn compile assembly:single

FROM eclipse-temurin:19.0.2_7-jre
WORKDIR /app
COPY --from=build /source/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
