
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /build

COPY pom.xml .
COPY dtos/pom.xml dtos/pom.xml
COPY microservices/push-worker/pom.xml microservices/push-worker/pom.xml

RUN mvn -B dependency:go-offline \
    -pl microservices/push-worker -am

COPY dtos/src dtos/src
COPY microservices/push-worker/src microservices/push-worker/src

RUN mvn -B clean package -DskipTests \
    -pl microservices/push-worker -am

FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S appgroup && \
    adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=build \
    /build/microservices/push-worker/target/*.jar \
    app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

USER appuser

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
