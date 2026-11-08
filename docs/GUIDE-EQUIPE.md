# Guide de l'équipe

SkillPath Audit : par où commencer. Master 1 MIAGE, Université Côte d'Azur. Version du 8 novembre 2026.

Ce guide est le point d'entrée du projet. Il se lit en une fois, avant la première tâche, puis sert de référence. Il renvoie vers les autres documents au lieu de les recopier : le dossier de cadrage (PDF), la [répartition](repartition.md), le [catalogue des lots](catalogue-des-lots.md) et le tableau du projet sur GitHub.

## Le projet en cinq lignes

- **Problème.** Les responsables de formation font évoluer leurs programmes sans outil qui confronte ce qu'ils enseignent à ce que demande le marché.
- **Solution.** SkillPath Audit relie les modules du M1 MIAGE à des compétences (ESCO complété d'une liste locale), mesure la demande dans les offres d'alternance du domaine systèmes d'information (famille ROME M18) et calcule un écart explicable, avec les preuves.
- **Ce que l'outil calcule.** La demande D est la part des offres du segment qui mentionnent une compétence (une mention n'est pas une exigence). La couverture K est le meilleur niveau atteint par un module (0 non traitée, 1 mentionnée, 2 pratiquée, 3 évaluée), divisé par 3. L'écart est G = D × (1 − K), toujours affiché avec D, K, les intervalles de Wilson et l'effectif.
- **Première version.** Collecte quotidienne, demande par segment, couverture et écart avec preuves, évaluation de l'extraction. Pas de tendances (elles demandent six mois de collecte).
- **Échéance.** Soutenance le mercredi 20 janvier 2027. Encadrant : M. Donati, responsable du M1 MIAGE.

## Ce qu'on construit

Un monolithe modulaire côté serveur (Spring Boot 3.5.16, Spring Modulith 1.4.13, Java 25), une base PostgreSQL 18, une application web Angular 22 servie en fichiers statiques, le tout lancé par Docker Compose. Un petit service Python d'embeddings est un candidat conditionnel : il n'existera que si les mesures le justifient.

| Module | Rôle | Dépend de |
|---|---|---|
| Ingestion | Récupère les offres (export de La Bonne Alternance), filtre sur le domaine, déduplique, garde le texte exploitable et le journal des collectes | Référentiel |
| Référentiel | Compétences d'ESCO et de la liste locale, alias, domaine et codes ROME, référentiel de la formation, table de correspondance | personne |
| Programmes | Formations, versions (brouillon, publié), unités d'enseignement, modules, niveaux de traitement | Référentiel, Extraction |
| Extraction | Détecte les compétences dans un texte (dictionnaire, embeddings conditionnels), mentions, jeu annoté, évaluation | Référentiel, Ingestion |
| Analytique | Demande, couverture, écart, instantanés, volume de collecte, export CSV | Ingestion, Extraction, Référentiel, Programmes |
| Accès | Comptes et rôles | personne |
| Pipeline | Travail unique de nuit : appelle Ingestion, Extraction puis Analytique | Ingestion, Extraction, Analytique |

Un module n'accède aux autres que par leur interface publique, sans cycle ; Spring Modulith le vérifie dans un test de la CI. Le front, le service Python et un petit module commun (configuration, erreurs, journalisation) complètent le tableau. Le détail est au chapitre 5 du dossier et dans [la vue d'ensemble](architecture/vue-densemble.md).

## Vocabulaire

| Terme | Sens |
|---|---|
| Segment | Le périmètre d'un chiffre : un domaine, un lieu (la France entière au départ) et une période (le mois de création de l'offre) |
| Demande | « Mentionnée dans X % des offres du segment » (offres dédupliquées, avec un texte de 200 caractères ou plus) |
| Couverture | Meilleur niveau auquel un module traite la compétence |
| Écart | Compétence très demandée et peu couverte |
| Module, matière, UE | Un module est une matière (23 dans le M1) ; une UE (7 dans le M1) regroupe des modules |
| Mention | Citation d'une compétence dans un texte |
| Annonce multi-sites | Même texte et même employeur publiés pour plusieurs lieux : offres distinctes, groupées par l'empreinte du texte |
| Microservice | Application autonome avec sa propre base, son déploiement et son équipe. Plusieurs conteneurs ne font pas des microservices |
| Idempotent | Rejouer le traitement donne le même résultat, sans doublon |

## Les données

Les offres viennent de l'export quotidien de La Bonne Alternance : 370 361 éléments le 7 novembre, dont 13 384 vraies offres (96 % sont des entreprises spontanées, sans texte). La famille M18 compte 146 offres, dont 129 avec texte. Les exports sont privés et ne sont **jamais** dans le dépôt.

| Niveau | Contenu | Pour qui |
|---|---|---|
| Synthétique | Jeux d'exemple inventés, dans le dépôt | Tout le monde |
| Extrait du domaine | Offres de la famille M18, textes masqués, dans un dossier partagé privé | Ingestion, Extraction, annotateurs |
| Résultats dérivés | Compétences extraites, instantanés, sans texte | Analytique, interface |
| Export complet | 700 Mo, hors dépôt | Une seule personne |

Dossier partagé privé : à définir dans le groupe. Aucune clé d'API ni donnée privée dans le dépôt.

## Installer et lancer

Prérequis : Git et Docker Desktop. Pour vérifier le serveur, depuis le dossier `backend`, sans rien installer d'autre :

```
docker run --rm -v "${PWD}:/work" -w /work maven:3.9-eclipse-temurin-25 mvn -B verify
```

Le test `ModularityTests` liste les modules détectés et échoue si une frontière est franchie. Pour développer dans un éditeur, installer Java 25 (repli Java 21) ; Node sera nécessaire avec l'application Angular. Le démarrage de l'ensemble (base, serveur, interface) avec des données d'exemple en une seule commande viendra avec les lots Plateforme et Données de développement.

## Comment on travaille

- Une branche par sujet, une pull request pour chaque modification, relue par au moins un autre membre avant la fusion.
- Les tests passent, dont la vérification des frontières de modules.
- Si l'architecture change, on écrit ou on met à jour une fiche de décision (dossier `docs/adr` du dépôt). Les structures proposées sont un point de départ, pas une règle sacrée.
- Aucun secret (clé, mot de passe) et aucune donnée privée dans le dépôt.
- **« Terminé »** : le code est relu, les tests passent, la documentation concernée est à jour, des données d'exemple permettent de lancer le projet, une démonstration est possible.
- Le fichier [CONTRIBUTING.md](../CONTRIBUTING.md) précise les détails.

## Où lire

- **Dossier de cadrage (PDF).** Chapitre 3 : définitions (demande, couverture, écart) ; chapitre 4 : sources de données ; chapitre 5 : architecture et modules ; chapitre 6 : modèle de données ; chapitre 7 : extraction et évaluation ; annexe : fiches de décision.
- **[Répartition](repartition.md)** et **[catalogue des lots](catalogue-des-lots.md)** : qui prend quoi, et ce qu'il faut faire dans chaque lot.
- Le tableau du projet SkillPath Audit sur GitHub et le dossier [docs/adr](adr/README.md) du dépôt.

## Qui contacter

Abdellah JEBAR, Abdelouarite GHILAN EL HASSANI, Maimouna BAH, Bouchra MEZRHAB, Elhadj Oumar DIALLO. Encadrant : M. Donati. Pour une question sur un lot, écrire dans le groupe : la réponse profite à tout le monde.
