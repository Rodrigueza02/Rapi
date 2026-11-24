FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

COPY . .

RUN mvn -B -pl microservices/email-worker -am \
    clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S appgroup && \
    adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=build \
    /app/microservices/email-worker/target/email-worker-1.0.0.jar \
    /app/app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

USER appuser

EXPOSE 8088

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
