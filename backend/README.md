# Backend SkillPath Audit

Serveur : monolithe modulaire (Spring Boot 3.5.16, Spring Modulith 1.4, Java 25).

Ce squelette contient sept modules vides et un noyau commun. Chaque module a une interface
publique (`…Api.java`, vide pour l'instant) et déclare ses dépendances autorisées dans son
`package-info.java`. Le test `ModularityTests` vérifie les frontières.

## Construire et tester

Docker doit être lancé : les tests d'intégration démarrent un PostgreSQL 18 en conteneur
(Testcontainers). Avec Maven installé et Java 25 :

```
mvn verify
```

Sans installer Java ni Maven, avec Docker (depuis le dossier `backend`, vérifié sous Docker
Desktop pour Windows). Le conteneur de Maven commande Docker par son socket, d'où les deux
options en plus :

```
docker run --rm -v "${PWD}:/work" -v /var/run/docker.sock:/var/run/docker.sock -e TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal -w /work maven:3.9-eclipse-temurin-25 mvn -B verify
```

Sous Git Bash, préfixer par `MSYS_NO_PATHCONV=1` et écrire `$(pwd -W)` à la place de `${PWD}`.

`ModularityTests` affiche les modules détectés : `commun`, `referentiel`, `acces`, `ingestion`,
`extraction`, `programmes`, `analytique`, `pipeline`. `StackSmokeTest` valide la pile (PostgreSQL 18,
Flyway, Spring Data JDBC, JdbcClient, Spring Batch, registre d'événements de Spring Modulith) ; ses tables
et sa migration sont des ressources de test, aucune table métier n'existe encore.
`GestionnaireErreursTest` vérifie que les erreurs sortent au format RFC 9457 (`application/problem+json`) sans détail interne.
`JournalisationTest` vérifie les journaux JSON, l'identifiant de requête (`X-Request-Id`) et l'absence de secret ou de texte d'offre dans les journaux.

## Lancer le serveur avec la base de développement

Chaque membre a sa propre base PostgreSQL 18, lancée par Docker Compose ; sa structure vient des
migrations Flyway, appliquées au démarrage du serveur. Les données ne sont pas partagées : tout ce
dont l'équipe a besoin se recrée depuis le dépôt.

1. Une seule fois : copier `.env.example` vers `.env` et y choisir un mot de passe (`.env` n'est
   jamais versionné).
2. Lancer le serveur avec le profil `dev` : dans l'éditeur (variable `spring.profiles.active=dev`) ou
   avec `mvn spring-boot:run -Dspring-boot.run.profiles=dev`. Le serveur lance lui-même la base du
   fichier `compose.yaml` et s'y connecte (module `spring-boot-docker-compose`, actif seulement en
   profil `dev`, absent du jar de production).
3. La base reste allumée à l'arrêt du serveur : `docker compose stop` l'éteint, `docker compose up -d`
   la rallume sans le serveur.
4. Repartir de zéro : `docker compose down -v` efface la base et son volume.

Les tests et la CI n'utilisent pas cette base : ils lancent la leur (Testcontainers).

## Où mettre son code

Dans le paquet de son module (`fr.skillpath.<module>`). L'interface publique et les objets échangés
restent à la racine du paquet ; tout le reste va dans `internal/` (couches `web`, `application`,
`domain`, `infrastructure`). Un autre module n'appelle jamais `internal/`.

Voir le guide de l'équipe et la fiche de décision sur la structure interne.
