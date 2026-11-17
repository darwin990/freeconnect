# syntax=docker/dockerfile:1

# ---------- Etapa de build ----------
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace

# Cache de dependencias: se invalida solo cuando cambia el pom
COPY pom.xml .
RUN mvn -q -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -B -DskipTests package

# ---------- Etapa de runtime ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S freeconnect && adduser -S freeconnect -G freeconnect
COPY --from=build /workspace/target/freeconnect.jar app.jar
RUN mkdir -p /app/data/portfolio && chown -R freeconnect:freeconnect /app
USER freeconnect

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=docker \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport"

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
