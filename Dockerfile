FROM maven:3.9.0-eclipse-temurin-19
FROM eclipse-temurin:19.0.2_7-jre
ENTRYPOINT ["cat"]
EXPOSE 8080