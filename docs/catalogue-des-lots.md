# Catalogue des lots

SkillPath Audit : une fiche de départ par lot. Version du 8 novembre 2026.

Ce catalogue décrit les quinze lots du projet, avec le même modèle pour chacun. Il se lit avec la [répartition](repartition.md) (qui prend quoi) et le [guide de l'équipe](GUIDE-EQUIPE.md). Le champ « Taille et responsable » est à compléter dans le groupe : S petit, M moyen, L gros (estimation relative, pas une durée). Les renvois « ch. n » désignent les chapitres du dossier de cadrage (PDF).

## Socle et plateforme

### Lot 1 : Squelette et noyau commun

| | |
|---|---|
| Objectif | Le socle sur lequel tous les modules se branchent. |
| Contenu | Sept modules vides avec dépendances autorisées déclarées ; test de vérification Spring Modulith ; PostgreSQL 18 avec Flyway (un dossier par module) ; module commun ouvert (configuration, gestion des erreurs, journalisation, audit), sans logique métier ; CI qui lance les tests. |
| Première tâche | Créer le projet Spring Boot 3.5.16 et valider que Spring Modulith 1.4, Spring Batch, Flyway, Java 25 et PostgreSQL 18 fonctionnent ensemble (repli Java 21). |
| Dépend de | Rien. |
| À lire | Ch. 5 (modules et dépendances), fiches [ADR-003](adr/adr-003.md), [ADR-008](adr/adr-008.md), [ADR-009](adr/adr-009.md), [ADR-017](adr/adr-017.md). |
| Terminé quand | La CI est verte ; le test de frontières passe ; un test de rejeu passe ; le projet démarre avec une seule commande. |
| Taille et responsable | M. Pris : Abdellah. |

### Lot 2 : Contrat d'API et client généré

| | |
|---|---|
| Objectif | Que l'interface et le serveur avancent en parallèle. |
| Contenu | Contrat OpenAPI versionné dans le dépôt ; client TypeScript généré ; serveur factice généré depuis le contrat ; test de la CI qui détecte toute dérive du contrat. |
| Première tâche | Écrire le contrat pour UC1 (voir les écarts d'un segment) et UC2 (ouvrir une compétence et voir les preuves). |
| Dépend de | Lot 1. |
| À lire | Ch. 5 et ch. 8, fiche [ADR-012](adr/adr-012.md). |
| Terminé quand | Le contrat est exporté et versionné ; le client se génère ; un serveur factice répond ; le test de dérive passe. |
| Taille et responsable | S. Pris : Abdellah. |

### Lot 3 : Plateforme

| | |
|---|---|
| Objectif | Lancer le projet de la même façon partout. |
| Contenu | Docker Compose de développement (serveur web, application, base) avec un profil sans le conteneur d'embeddings ; images construites par la CI ; règles de migration Flyway (numérotation par module, toute migration en pull request). |
| Première tâche | Écrire le fichier Compose de développement et le profil sans embeddings. |
| Dépend de | Lot 1. |
| À lire | Ch. 5 (déploiement), fiches [ADR-014](adr/adr-014.md), [ADR-017](adr/adr-017.md). |
| Terminé quand | Une commande lance l'ensemble ; la CI construit les images ; les règles de migration sont écrites. |
| Taille et responsable | M. Pris : Abdellah. |

### Lot 4 : Pipeline

| | |
|---|---|
| Objectif | Enchaîner la collecte, l'extraction et le calcul chaque nuit, sans doublon. |
| Contenu | Un travail Spring Batch unique appelle Ingestion, Extraction puis Analytique ; paramètres d'identification fixes (date de collecte, source) ; reprise à l'étape en échec ; deux alertes (échec d'étape, aucune collecte réussie depuis 26 heures). |
| Première tâche | Écrire le squelette du travail avec des étapes factices et ses paramètres d'identification. |
| Dépend de | Lots 5, 8 et 10 (leurs interfaces). |
| À lire | Ch. 5 (flux et scénarios), fiche [ADR-009](adr/adr-009.md). |
| Terminé quand | Relancer avec les mêmes paramètres reprend à l'étape en échec sans doublon ; un double lancement est impossible ; les alertes sont testées. |
| Taille et responsable | S. Pris : Abdellah. |

## Collecte et référentiel

### Lot 5 : Ingestion

| | |
|---|---|
| Objectif | Récupérer les offres de façon fiable et ne garder que ce qui sert. |
| Contenu | Adaptateur de la source (export de La Bonne Alternance) lu en flux ; téléchargement reprenable (trois essais) avec contrôle de complétude ; filtre du domaine ; déduplication (doublon exact avec le lieu, annonce multi-sites, quasi-doublon à 0,9 de Jaccard sur cinq mots) ; texte exploitable avec masquage des courriels et téléphones ; brut filtré ; journal des collectes. |
| Première tâche | Lire un export en flux et compter les vraies offres et les entreprises spontanées. |
| Dépend de | Lots 1 et 6 (domaine). |
| À lire | Ch. 4 (sources), ch. 6 (6.1 à 6.3), fiches [ADR-009](adr/adr-009.md), [ADR-011](adr/adr-011.md), [ADR-025](adr/adr-025.md). |
| Terminé quand | La collecte est rejouable sans doublon ; un fichier tronqué est rejeté ; les tests passent sur les jeux d'exemple ; le journal est écrit. |
| Taille et responsable | L. À choisir. |

### Lot 6 : Référentiel

| | |
|---|---|
| Objectif | Fournir le vocabulaire de compétences commun à tout le système. |
| Contenu | Chargement d'ESCO v1.2.1 ; alias français et anglais, alias écartés ; liste locale (fichier versionné, avec identifiants externes) ; domaine (famille M18) et codes ROME ; référentiel de la formation (blocs, énoncés) ; table de correspondance avec relations, confiance, auteur et version. |
| Première tâche | Charger les fichiers d'ESCO et retrouver les compétences nommées du test de couverture (Python, SQL, Java, PostgreSQL, etc.). |
| Dépend de | Lot 1. |
| À lire | Ch. 3 (taxonomie), ch. 6, fiches [ADR-002](adr/adr-002.md), [ADR-006](adr/adr-006.md), [ADR-007](adr/adr-007.md), [ADR-024](adr/adr-024.md). |
| Terminé quand | ESCO est chargé et versionné ; la liste locale se charge depuis un fichier ; le domaine est versionné ; la table de correspondance a ses relations et sa provenance. |
| Taille et responsable | M. À choisir. |

## Programme de la formation

### Lot 7 : Programmes

| | |
|---|---|
| Objectif | Représenter la formation et ses versions, et ce que chaque module traite. |
| Contenu | Formations, versions (brouillon puis publié, périodes sans chevauchement), unités d'enseignement, modules, identifiant stable d'un module d'une version à l'autre ; liens module-compétence avec niveau et historique des corrections ; import (modules fictifs pour tester) ; correction par l'enseignant (UC6) ; publication (UC9). |
| Première tâche | Modéliser formation, version, UE et module, puis importer la structure de la fiche descriptive (7 UE, 23 matières). |
| Dépend de | Lots 1, 6 et 8 ; la maquette détaillée pour la couverture réelle. |
| À lire | Ch. 3 (couverture), ch. 6 (6.4), fiche [ADR-008](adr/adr-008.md). |
| Terminé quand | La structure s'importe ; un brouillon est modifiable avec historique ; la publication fige ; la contrainte de non-chevauchement est testée. |
| Taille et responsable | M. À choisir. |

## Extraction et IA

### Lot 8 : Extraction

| | |
|---|---|
| Objectif | Détecter les compétences dans un texte et mesurer la qualité de la détection. |
| Contenu | Dictionnaire en Java (mots entiers, terme le plus long, casse exacte pour les termes courts, jetons spéciaux, deux langues, racinisation) ; mentions avec passage et confiance ; banc d'évaluation (précision et rappel avec Wilson, F1 avec bootstrap par offre) ; garde-fou d'évaluation dans la CI. |
| Première tâche | Reprendre le banc exploratoire et mesurer le dictionnaire sur l'extrait du domaine. |
| Dépend de | Lots 6 et 5 ; l'extrait du domaine masqué. |
| À lire | Ch. 7, fiche [ADR-010](adr/adr-010.md). |
| Terminé quand | Le dictionnaire est mesuré sur le test gelé avec ses intervalles ; le garde-fou est en CI ; les mentions sont stockées. |
| Taille et responsable | L. À choisir. |

### Lot 9 : Service d'IA (conditionnel)

| | |
|---|---|
| Objectif | Savoir si les embeddings battent le dictionnaire, et les brancher seulement si c'est le cas. |
| Contenu | Conteneur Python d'embeddings (EmbeddingGemma, BGE-M3, multilingual-e5-large-instruct) : des phrases en entrée, des vecteurs en sortie ; sans base, sans état, versionné avec le dépôt ; mesure des trois modèles ; règle d'acceptation : gain d'au moins 5 points de F1 avec un intervalle bootstrap apparié excluant zéro. |
| Première tâche | Faire tourner EmbeddingGemma sur quelques phrases et relever vitesse et mémoire. |
| Dépend de | Lot 8 (dictionnaire mesuré) et lot 15 (jeu annoté). |
| À lire | Ch. 7 (amélioration par traitement du langage), fiche [ADR-010](adr/adr-010.md). |
| Terminé quand | Le gain est mesuré et la décision écrite dans une fiche (ADR-018 et ADR-019) : retenir ou non. |
| Taille et responsable | M. À choisir. |

### Lot 15 : Annotation

| | |
|---|---|
| Objectif | Fabriquer le jeu de référence qui permet toute mesure. |
| Contenu | Guide d'annotation testé sur 20 unités ; lots de 100 unités ; deux annotateurs sur au moins 20 % des unités (100 % au premier lot) ; accord rapporté (F1, pourcentage, kappa) ; arbitrage ; découpage par offre ; test gelé ; descriptions de modules relues par un enseignant. |
| Première tâche | Annoter 20 unités avec le guide et comparer les deux annotations. |
| Dépend de | L'extrait du domaine masqué et les feuilles d'annotation. |
| À lire | Ch. 7 (jeu de référence annoté) et l'annexe sur le guide d'annotation. |
| Terminé quand | Un jeu d'au moins 300 unités dont 150 de test ; accord rapporté ; test gelé et versionné. |
| Taille et responsable | M. Au moins deux personnes. |

## Résultats et interface

### Lot 10 : Analytique

| | |
|---|---|
| Objectif | Calculer la demande, la couverture et l'écart, avec leurs preuves. |
| Contenu | Demande avec intervalles de Wilson ; instantanés provisoires puis figés ; couverture et écart calculés à la demande ; volume de collecte ; export CSV ; formules en Java pur, testées sur des valeurs de référence. |
| Première tâche | Écrire les formules et leurs tests, dont l'exemple à la main (8, 20 et 12 offres sur 100). |
| Dépend de | Lots 8 et 7 ; peut commencer avec des données synthétiques. |
| À lire | Ch. 3, ch. 6 (6.5), fiches [ADR-008](adr/adr-008.md), [ADR-024](adr/adr-024.md), [ADR-026](adr/adr-026.md). |
| Terminé quand | Les tests de formule et de Wilson passent ; un instantané est rejouable ; l'effectif insuffisant est géré. |
| Taille et responsable | M. À choisir. |

### Lot 12 : Interface

| | |
|---|---|
| Objectif | Rendre le produit visible et utilisable. |
| Contenu | Cadre Angular (navigation, connexion, client généré) ; tableau de bord UC1 et UC2 ; écrans d'administration et de correction liés aux modules ; export CSV. Le choix de la bibliothèque de composants est libre et documenté dans une fiche de décision. |
| Première tâche | Générer le client à partir du contrat et afficher une page contre le serveur factice. |
| Dépend de | Lot 2. |
| À lire | Ch. 2 (cas d'utilisation), ch. 5, fiches [ADR-004](adr/adr-004.md), [ADR-012](adr/adr-012.md). |
| Terminé quand | UC1 et UC2 fonctionnent contre le serveur factice puis contre le serveur réel ; deux ou trois parcours sont testés. |
| Taille et responsable | L. À choisir. |

### Lot 11 : Accès

| | |
|---|---|
| Objectif | Protéger l'accès selon le rôle. |
| Contenu | Comptes et rôles (responsable, membre du conseil, enseignant, administrateur) ; Spring Security ; hachage Argon2id ; limitation des tentatives ; session limitée ; protection CSRF pour application monopage ; règle « l'enseignant ne corrige que ses modules » vérifiée avec l'identité de la session. |
| Première tâche | Configurer la sécurité et la protection CSRF avec un écran de connexion minimal. |
| Dépend de | Lot 1. |
| À lire | Ch. 8 (sécurité), fiche [ADR-013](adr/adr-013.md). |
| Terminé quand | Les tests par rôle passent ; la protection CSRF est testée avec Angular ; les mots de passe sont hachés. |
| Taille et responsable | S. À choisir. |

## Données, tests et documentation

### Lot 13 : Données de développement

| | |
|---|---|
| Objectif | Donner à chacun des données pour travailler, sans exposer de données privées. |
| Contenu | Jeux d'exemple synthétiques avec graine fixe ; jeu agrégé réaliste (sans texte) ; script de chargement ; dossier de données séparé des migrations de schéma, chargé seulement par le profil de développement. |
| Première tâche | Définir le format de l'extrait du domaine et écrire le script qui génère les jeux synthétiques. |
| Dépend de | Lot 1. |
| À lire | Ch. 6 et la section « Les données » du [guide](GUIDE-EQUIPE.md). |
| Terminé quand | Un nouveau membre charge des données d'exemple avec une commande ; aucun texte privé n'est dans le dépôt. |
| Taille et responsable | S. À choisir. |

### Lot 14 : Tests de bout en bout et documentation

| | |
|---|---|
| Objectif | Garantir que l'ensemble tient et que tout le monde s'y retrouve. |
| Contenu | Deux ou trois parcours de bout en bout (UC1, UC2, UC6) ; tests de conformité transversaux ; fiches de décision (ADR-018 à ADR-027) ; document d'interfaces des modules ; guide tenu à jour. |
| Première tâche | Relire le document d'interfaces et lister les tests de conformité à écrire. |
| Dépend de | Les autres lots. |
| À lire | Ch. 8 et l'annexe des fiches de décision. |
| Terminé quand | Les parcours tournent en CI ; les fiches sont écrites dans le dépôt ; le guide est à jour. |
| Taille et responsable | M. À choisir. |
