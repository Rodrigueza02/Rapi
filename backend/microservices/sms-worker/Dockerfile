
# ============ Etapa 1: compilación ============
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /build

# POM padre y módulos activos
COPY pom.xml .
COPY dtos/pom.xml dtos/pom.xml
COPY microservices/order-api/pom.xml microservices/order-api/pom.xml
COPY microservices/push-worker/pom.xml microservices/push-worker/pom.xml
COPY microservices/sms-worker/pom.xml microservices/sms-worker/pom.xml

# Descargar dependencias
RUN mvn -B dependency:go-offline \
    -pl microservices/sms-worker -am

# Copiar fuentes necesarias
COPY dtos/src dtos/src
COPY microservices/sms-worker/src microservices/sms-worker/src

# Compilar el módulo y sus dependencias
RUN mvn -B clean package -DskipTests \
    -pl microservices/sms-worker -am

# ============ Etapa 2: ejecución ligera ============
FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S appgroup && \
    adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=build \
    /build/microservices/sms-worker/target/*.jar \
    app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

USER appuser

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
