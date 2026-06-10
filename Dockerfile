# ── Étape 1 : Build du frontend React ────────────────────────────────────────
FROM node:20-slim AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci --silent
COPY frontend/ ./
RUN npm run build

# ── Étape 2 : Build du backend Spring Boot ───────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS backend-build
WORKDIR /app

# Télécharge les dépendances Maven en cache séparé
COPY pom.xml ./
RUN mvn dependency:go-offline -q

# Copie les sources
COPY src ./src

# Copie le frontend compilé dans les ressources statiques de Spring Boot
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static

# Build du JAR (sans tests pour accélérer)
RUN mvn package -DskipTests -q

# ── Étape 3 : Image de production ────────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
