FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /build

COPY pom.xml .
COPY dtos/pom.xml dtos/pom.xml
COPY microservices/order-api/pom.xml microservices/order-api/pom.xml
COPY microservices/order-processor/pom.xml microservices/order-processor/pom.xml
COPY microservices/driver-simulator/pom.xml microservices/driver-simulator/pom.xml
COPY microservices/notification-router/pom.xml microservices/notification-router/pom.xml
COPY microservices/sms-worker/pom.xml microservices/sms-worker/pom.xml
COPY microservices/push-worker/pom.xml microservices/push-worker/pom.xml
COPY microservices/dashboard-aggregator/pom.xml microservices/dashboard-aggregator/pom.xml
COPY microservices/dashboard-backend/pom.xml microservices/dashboard-backend/pom.xml
COPY microservices/auth-service/pom.xml microservices/auth-service/pom.xml
COPY microservices/email-worker/pom.xml microservices/email-worker/pom.xml

RUN mvn -B dependency:go-offline -pl microservices/driver-simulator -am || true

COPY dtos/src dtos/src
COPY microservices/driver-simulator/src microservices/driver-simulator/src
RUN mvn -B clean package -DskipTests -pl microservices/driver-simulator -am

FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app

COPY --from=build /build/microservices/driver-simulator/target/*.jar app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

USER appuser
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]