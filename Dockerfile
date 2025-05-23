FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app
    
COPY pom.xml .
RUN mvn dependency:go-offline -B
    
COPY . .
     
RUN mvn clean package -DskipTests
    
FROM eclipse-temurin:17-jre-alpine
    
RUN addgroup -S spring && adduser -S spring -G spring

USER root

WORKDIR /app
    
COPY --from=builder /app/target/demo-0.0.1-SNAPSHOT.jar app.jar
    
COPY entrypoint.sh /app/entrypoint.sh

RUN chmod +x /app/entrypoint.sh

USER spring:spring

ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE=default
    
EXPOSE 4500
    
ENTRYPOINT ["/app/entrypoint.sh"]
