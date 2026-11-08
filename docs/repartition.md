# Répartition du travail

SkillPath Audit : les lots et qui prend quoi. Version du 8 novembre 2026.

## Principe

Le projet est découpé en **quinze lots**. Chacun choisit les siens dans le groupe. Chaque lot a **un responsable** et **un suppléant** (quelqu'un qui sait le reprendre). Un responsable possède son lot de bout en bout : le code serveur, ses tables, ses écrans et ses tests. La colonne « Taille » est une estimation relative du travail (S petit, M moyen, L gros), pas une durée.

## Les lots

| N° | Lot | Contenu | Dépend de | Taille | Responsable | Suppléant | Statut |
|---:|---|---|---|---|---|---|---|
| 1 | Squelette et noyau commun | Sept modules vides avec dépendances déclarées, test de frontières Spring Modulith, PostgreSQL 18 et Flyway, module commun (configuration, erreurs, journalisation), CI | rien | M | Abdellah | à choisir | Pris |
| 2 | Contrat d'API et client généré | Contrat OpenAPI versionné, client TypeScript généré, serveur factice, test de dérive | 1 | S | Abdellah | à choisir | Pris |
| 3 | Plateforme | Docker Compose (avec et sans le service Python), CI, images, règles de migration | 1 | M | Abdellah | à choisir | Pris |
| 4 | Pipeline | Travail unique de nuit (Ingestion, Extraction, Analytique), reprise, deux alertes | 5, 8, 10 | S | Abdellah | à choisir | Pris |
| 5 | Ingestion | Adaptateur de source, téléchargement reprenable, filtre du domaine, déduplication, texte exploitable, journal | 1, 6 | L | à choisir | à choisir | À choisir |
| 6 | Référentiel | ESCO, alias, liste locale, domaine, référentiel de la formation, table de correspondance | 1 | M | à choisir | à choisir | À choisir |
| 7 | Programmes | Formations, versions, UE, modules, niveaux, import, correction par l'enseignant, publication | 1, 6, 8 | M | à choisir | à choisir | À choisir |
| 8 | Extraction | Dictionnaire, mentions, banc d'évaluation, garde-fou de CI | 6, 5 | L | à choisir | à choisir | À choisir |
| 9 | Service d'IA | Conteneur Python d'embeddings, mesure de trois modèles, règle des 5 points de F1 (conditionnel) | 8, 15 | M | à choisir | à choisir | À choisir |
| 10 | Analytique | Demande, intervalles de Wilson, instantanés, couverture, écart, volume de collecte, export CSV | 8, 7 | M | à choisir | à choisir | À choisir |
| 11 | Accès | Comptes, rôles, sécurité, protection CSRF pour application monopage | 1 | S | à choisir | à choisir | À choisir |
| 12 | Interface | Cadre Angular, tableau de bord (écarts et preuves), écrans d'administration et de correction | 2 | L | à choisir | à choisir | À choisir |
| 13 | Données de développement | Jeux d'exemple synthétiques, jeu agrégé réaliste, script de chargement | 1 | S | à choisir | à choisir | À choisir |
| 14 | Tests de bout en bout et documentation | Parcours de bout en bout, tests de conformité transversaux, fiches de décision, document d'interfaces | tous | M | à choisir | à choisir | À choisir |
| 15 | Annotation | Guide testé, lots de 100 unités, deux annotateurs, accord, arbitrage, test gelé | extrait du domaine | M | à choisir | à choisir | À choisir |

## Qui fait quoi

**R** = responsable du lot (un seul par lot) ; **S** = suppléant ; **C** = consulté ; **I** = informé. Les lignes déjà prises sont remplies ; le reste est à remplir dans le groupe.

| N° | Lot | Abdellah | Abdelouarite | Maimouna | Bouchra | Elhadj |
|---:|---|:-:|:-:|:-:|:-:|:-:|
| 1 | Squelette et noyau commun | R | | | | |
| 2 | Contrat d'API et client généré | R | | | | |
| 3 | Plateforme | R | | | | |
| 4 | Pipeline | R | | | | |
| 5 | Ingestion | | | | | |
| 6 | Référentiel | | | | | |
| 7 | Programmes | | | | | |
| 8 | Extraction | | | | | |
| 9 | Service d'IA | | | | | |
| 10 | Analytique | | | | | |
| 11 | Accès | | | | | |
| 12 | Interface | | | | | |
| 13 | Données de développement | | | | | |
| 14 | Tests de bout en bout et documentation | | | | | |
| 15 | Annotation | | | | | |

## Ordre de démarrage

1. **D'abord, indépendants de tout :** la collecte quotidienne (chaque jour sans collecte est une journée d'historique perdue), la demande de la maquette détaillée du M1 à M. Donati, la lecture du guide et le questionnaire.
2. **Le squelette (lot 1)** : il bloquait tout le monde ; il est fusionné. Il valide que Spring Boot 3.5.16, Spring Modulith 1.4 et Java 25 fonctionnent ensemble ; Spring Batch, Flyway et PostgreSQL 18 restent à valider avec leurs lots.
3. **Maintenant, en parallèle :** Référentiel (6), Ingestion (5), guide d'annotation et banc d'évaluation (15 et 8), cadre de l'interface sur serveur factice (12) une fois le contrat écrit (2).
4. **Ensuite :** Extraction branchée sur Ingestion, Analytique, Programmes (couverture quand la maquette arrive), puis l'essai des embeddings (9), borné dans le temps.

## Règles pour prendre un lot

- Un lot critique avant un lot confortable : le squelette, la collecte, le contrat d'API, le jeu annoté et le chargement d'ESCO comptent le plus.
- Au plus deux lots lourds (L) par personne.
- Un suppléant pour chaque lot, qui assiste aux revues du lot.
- Pour échanger un lot, on le dit dans le groupe et on met le tableau à jour.
- Le choix de la bibliothèque de composants de l'interface est libre, documenté dans une fiche de décision.

## Lots déjà pris

Abdellah : squelette et noyau commun (1), contrat d'API (2), plateforme (3), Pipeline (4). Les autres sont à choisir dans le groupe, avec le [catalogue des lots](catalogue-des-lots.md) comme guide.
