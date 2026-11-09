# ---------- Estagio 1: build ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copia so o pom primeiro para aproveitar o cache de dependencias do Docker
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Estagio 2: runtime ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring \
    && mkdir -p /app/uploads && chown -R spring:spring /app
USER spring

COPY --from=build /app/target/*.jar app.jar

ENV UPLOADS_DIR=/app/uploads
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
