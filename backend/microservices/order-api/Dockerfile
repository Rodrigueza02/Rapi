
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml ./pom.xml
COPY dtos ./dtos
COPY microservices/order-api ./microservices/order-api

RUN mvn -B -pl microservices/order-api -am \
    clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

RUN useradd --system --uid 10001 appuser

COPY --from=build \
    /app/microservices/order-api/target/*.jar \
    app.jar

USER appuser

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
