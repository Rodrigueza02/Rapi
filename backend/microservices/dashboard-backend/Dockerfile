
# ETAPA 1: Compilar con Maven y Java 17
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copiamos el proyecto Maven completo.
# El POM principal necesita encontrar todos los modulos.
COPY . .

# Compilamos solo dashboard-backend y sus dependencias.
RUN mvn -B -pl microservices/dashboard-backend -am \
    clean package -DskipTests

# ETAPA 2: Ejecutar con Java 17
FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S appgroup && \
    adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=build \
    /app/microservices/dashboard-backend/target/dashboard-backend-1.0.0.jar \
    /app/app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

USER appuser

EXPOSE 8086

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
