# 1. Builder
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Carga dependencias sin compilar código
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia sólo el código fuente
COPY src ./src

# Genera el JAR
RUN mvn clean package -DskipTests

# 2. Runtime
FROM eclipse-temurin:17-jre-alpine AS runtime

# Usuario no-root
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app

# Copia el JAR (wildcard para evitar nombre fijo)
COPY --from=builder /app/target/*.jar app.jar

# Copia entrypoint y dale permisos
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# Cambia a usuario spring
USER spring:spring

# Variables de entorno
ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}
EXPOSE ${SPRING_APP_PORT}

# Arranque de la aplicación
ENTRYPOINT ["/app/entrypoint.sh"]