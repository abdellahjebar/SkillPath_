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
| 6 | Référentiel | ESCO, alias, liste locale, domaine, référentiel de la formation, table de correspondance | 1 | M | Bouchra | à choisir | Pris |
| 7 | Programmes | Formations, versions, UE, modules, niveaux, import, correction par l'enseignant, publication | 1, 6, 8 | M | Elhadj Oumar | à choisir | Pris |
| 8 | Extraction | Dictionnaire, mentions, banc d'évaluation, garde-fou de CI | 6, 5 | L | à choisir | à choisir | À choisir |
| 9 | Service d'IA | Conteneur Python d'embeddings, mesure de trois modèles, règle des 5 points de F1 (conditionnel) | 8, 15 | M | Bouchra | à choisir | Pris |
| 10 | Analytique | Demande, intervalles de Wilson, instantanés, couverture, écart, volume de collecte, export CSV | 8, 7 | M | Elhadj Oumar | à choisir | Pris |
| 11 | Accès | Comptes, rôles, sécurité, protection CSRF pour application monopage | 1 | S | à choisir | à choisir | À choisir |
| 12 | Interface | Cadre Angular, tableau de bord (écarts et preuves), écrans d'administration et de correction | 2 | L | Maimouna | à choisir | Pris |
| 13 | Données de développement | Jeux d'exemple synthétiques, jeu agrégé réaliste, script de chargement | 1 | S | Elhadj Oumar | à choisir | Pris |
| 14 | Tests de bout en bout et documentation | Parcours de bout en bout, tests de conformité transversaux, fiches de décision, document d'interfaces | tous | M | à choisir | à choisir | À choisir |
| 15 | Annotation | Guide testé, lots de 100 unités, deux annotateurs, accord, arbitrage, test gelé | extrait du domaine | M | Maimouna | à choisir | Pris |

## Qui fait quoi

**R** = responsable du lot (un seul par lot) ; **S** = suppléant ; **C** = consulté ; **I** = informé. Les lignes déjà prises sont remplies ; le reste est à remplir dans le groupe.

| N° | Lot | Abdellah | Abdelouarite | Maimouna | Bouchra | Elhadj |
|---:|---|:-:|:-:|:-:|:-:|:-:|
| 1 | Squelette et noyau commun | R | | | | |
| 2 | Contrat d'API et client généré | R | | | | |
| 3 | Plateforme | R | | | | |
| 4 | Pipeline | R | | | | |
| 5 | Ingestion | | | | | |
| 6 | Référentiel |  |  |  | R |  |
| 7 | Programmes |  |  |  |  | R |
| 8 | Extraction | | | | | |
| 9 | Service d'IA |  |  |  | R |  |
| 10 | Analytique |  |  |  |  | R |
| 11 | Accès | | | | | |
| 12 | Interface |  |  | R |  |  |
| 13 | Données de développement |  |  |  |  | R |
| 14 | Tests de bout en bout et documentation | | | | | |
| 15 | Annotation |  |  | R |  |  |

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

- Abdellah : squelette et noyau commun (1), contrat d'API (2), plateforme (3), Pipeline (4).
- Maimouna : Interface (12) et Annotation (15).
- Bouchra : Référentiel (6) et Service d'IA (9).
- Elhadj Oumar : Programmes (7), Analytique (10) et Données de développement (13).

Restent à choisir dans le groupe : Ingestion (5), Extraction (8), Accès (11) et Tests de bout en bout et documentation (14), avec le [catalogue des lots](catalogue-des-lots.md) comme guide. Les suppléants sont aussi à choisir.

## Travaux hors lots

Livrés avant ou en dehors des quinze lots, pour mémoire. Faits datés, avec renvoi vers la trace dans le dépôt.

| Travail | Date | Trace |
|---|---|---|
| Mise en place du dépôt : modèles d'issues et de pull request, propriétaires de code, intégration continue d'hygiène | octobre 2026 | PR #1, #2 et #4 |
| Dossier de cadrage, version 0.3 (78 pages) : besoin, définitions, sources, architecture, modèle de données, extraction, annexes | octobre et novembre 2026 | dossier hors dépôt, ADR extraits dans `docs/adr/` |
| Lecture critique du dossier et corrections qui en sont sorties | novembre 2026 | ADR 001 à 017 révisées, ADR 020 à 027 |
| Analyse des exports de La Bonne Alternance (370 361 éléments, 13 384 offres, famille M18) et premier appel d'export | 1 et 7 novembre 2026 | issues #5 et #6 |
| Squelette du serveur : sept modules et test de frontières | 8 novembre 2026 | PR #7, issue #3 |
| Mise à jour de la documentation : README, architecture, 25 fiches de décision (001 à 017, 020 à 027), guide, répartition, catalogue des lots | 8 novembre 2026 | PR #8 |
| Contrat OpenAPI de la première version et serveur factice | 21 novembre 2026 | PR #16 |

Ce tableau se complète au fil du projet : chacun peut y ajouter ce qu'il livre hors de ses lots.
