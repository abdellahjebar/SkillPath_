# Backend SkillPath Audit

Serveur : monolithe modulaire (Spring Boot 3.5.16, Spring Modulith 1.4, Java 25).

Ce squelette contient sept modules vides et un noyau commun. Chaque module a une interface
publique (`…Api.java`, vide pour l'instant) et déclare ses dépendances autorisées dans son
`package-info.java`. Le test `ModularityTests` vérifie les frontières.

## Construire et tester

Avec Maven installé et Java 25 :

```
mvn verify
```

Sans rien installer, avec Docker (depuis le dossier `backend`) :

```
docker run --rm -v "${PWD}:/work" -w /work maven:3.9-eclipse-temurin-25 mvn -B verify
```

Le test affiche les modules détectés : `commun`, `referentiel`, `acces`, `ingestion`,
`extraction`, `programmes`, `analytique`, `pipeline`.

## Où mettre son code

Dans le paquet de son module (`fr.skillpath.<module>`). L'interface publique et les objets échangés
restent à la racine du paquet ; tout le reste va dans `internal/` (couches `web`, `application`,
`domain`, `infrastructure`). Un autre module n'appelle jamais `internal/`.

Voir le guide de l'équipe et la fiche de décision sur la structure interne.
