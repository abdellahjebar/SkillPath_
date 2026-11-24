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

## Où mettre son code

Dans le paquet de son module (`fr.skillpath.<module>`). L'interface publique et les objets échangés
restent à la racine du paquet ; tout le reste va dans `internal/` (couches `web`, `application`,
`domain`, `infrastructure`). Un autre module n'appelle jamais `internal/`.

Voir le guide de l'équipe et la fiche de décision sur la structure interne.
