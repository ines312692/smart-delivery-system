# syntax=docker/dockerfile:1.7

# -------- Build stage --------
FROM maven:3.9-eclipse-temurin-24 AS build
WORKDIR /workspace

COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY monitoring-service/pom.xml monitoring-service/pom.xml

RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests -pl monitoring-service -am dependency:go-offline

COPY . .
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests -pl monitoring-service -am package

# -------- Runtime stage --------
FROM gcr.io/distroless/java24-debian12:nonroot

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:InitialRAMPercentage=50 -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE=prod

WORKDIR /app
COPY --from=build /workspace/monitoring-service/target/*.jar /app/app.jar

EXPOSE 8080
USER nonroot
ENTRYPOINT ["java","-jar","/app/app.jar"]