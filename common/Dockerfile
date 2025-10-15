# syntax=docker/dockerfile:1.7

# -------- Build stage (compile le JAR du module common) --------
FROM maven:3.9-eclipse-temurin-24 AS build
WORKDIR /workspace

# Copier uniquement les POM nécessaires pour maximiser le cache
COPY pom.xml .
COPY common/pom.xml common/pom.xml

# Précharger les dépendances
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests -pl common -am dependency:go-offline

# Copier le code et construire l'artefact
COPY . .
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests -pl common -am package

# -------- Artifact image (transport d'artefact, non exécutable) --------
FROM scratch
LABEL org.opencontainers.image.title="common-artifact" \
      org.opencontainers.image.description="Artefact du module common (bibliothèque), non exécutable" \
      org.opencontainers.image.source="." \
      org.opencontainers.image.licenses="Apache-2.0"

# Expose le JAR compilé dans /artifacts
COPY --from=build /workspace/common/target/*.jar /artifacts/common.jar

# Pas d'ENTRYPOINT: cette image sert uniquement de porte-artefact.