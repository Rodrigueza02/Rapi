FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copiar todos los modulos del proyecto Maven actual
COPY . .

# Compilar el microservicio y sus dependencias
RUN mvn -B -pl microservices/sms-worker -am clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=build /app/microservices/sms-worker/target/sms-worker-1.0.0.jar /app/app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

USER appuser

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
