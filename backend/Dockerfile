# Image du serveur SkillPath. Construction depuis les sources : ni Java ni Maven à installer.
#   docker build -t skillpath-backend backend
# Les tests ne sont pas lancés ici : ils le sont par la CI (mvn verify).

# Étape 1 : construire le jar et le découper en couches (documentation de Spring Boot 3.5,
# « Dockerfiles » : java -Djarmode=tools ... extract --layers).
FROM maven:3.9-eclipse-temurin-25 AS construction
WORKDIR /construction
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -DskipTests package \
    && cp target/skillpath-backend-*.jar application.jar \
    && java -Djarmode=tools -jar application.jar extract --layers --destination /extrait

# Étape 2 : l'image d'exécution, avec le seul JRE. Les couches qui changent le moins sont copiées
# en premier pour que Docker réutilise son cache.
FROM eclipse-temurin:25-jre
RUN useradd --system --uid 10001 --no-create-home skillpath
WORKDIR /application
COPY --from=construction /extrait/dependencies/ ./
COPY --from=construction /extrait/spring-boot-loader/ ./
COPY --from=construction /extrait/snapshot-dependencies/ ./
COPY --from=construction /extrait/application/ ./
# Utilisateur sans droits d'administration (bonnes pratiques de Docker).
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "application.jar"]
