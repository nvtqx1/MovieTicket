FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY certs ./certs

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
COPY --from=build /app/certs ./certs

ENV PORT=10000

EXPOSE 10000

CMD ["sh", "-c", "java -jar app.jar"]