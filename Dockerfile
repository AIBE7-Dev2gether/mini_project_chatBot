FROM maven:3-eclipse-temurin-17 AS builder

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=builder /app/target/*.war /app/archat.war

EXPOSE 10000

ENTRYPOINT ["java", "-jar", "/app/archat.war"]
