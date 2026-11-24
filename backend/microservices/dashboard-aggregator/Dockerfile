
# ETAPA 1: Compilar con Maven y Java 17
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Incluye los POM y fuentes de todos los modulos.
COPY . .

# Maven compila dtos y dashboard-aggregator.
RUN mvn -B -pl microservices/dashboard-aggregator -am \
    clean package -DskipTests

# ETAPA 2: Ejecutar con Java 17
FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S appgroup && \
    adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=build \
    /app/microservices/dashboard-aggregator/target/dashboard-aggregator-1.0.0.jar \
    /app/app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

USER appuser

EXPOSE 8085

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
