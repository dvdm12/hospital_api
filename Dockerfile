FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copiamos el pom y las dependencias primero 
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos el resto del código fuente
COPY . .

# Compilamos el proyecto (con test deshabilitado para velocidad)
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine

# Crea un usuario no root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

COPY --from=builder /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

# Variables para configurar en tiempo de ejecución
ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE=default

EXPOSE 4500

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
