# SkillPath Audit

Mesurer l'écart entre les compétences demandées par le marché de l'emploi et celles enseignées dans une formation, de façon traçable et vérifiable.

**Statut : cadrage, dossier v0.2 (deuxième rédaction)** · Master 1 MIAGE, Université Côte d'Azur · 2026-2027

---

## Le problème

Faire évoluer le programme d'une formation repose aujourd'hui sur des impressions et des échanges ponctuels avec des professionnels. Les responsables de formation n'ont pas d'outil simple pour répondre à une question précise : **quelles compétences très demandées ne sont pas enseignées dans notre parcours, et cette demande évolue-t-elle ?**

## Ce que SkillPath Audit vise à faire

- **Normaliser les programmes** : rattacher les modules d'une formation à un référentiel de compétences (ESCO), avec le détail de chaque rattachement.
- **Mesurer la demande** : estimer le poids de chaque compétence sur le marché, par métier, domaine, lieu et période.
- **Calculer un écart explicable** : une formule définie, testée, et qui renvoie aux modules et aux offres qui la justifient.
- **Suivre les tendances** : comparer les périodes sur des agrégats figés et reproductibles.

> Ce que l'outil **ne fait pas** : il ne recommande pas de cours et ne remplace pas la décision pédagogique. Il apporte des éléments de preuve à ceux qui la prennent.

## Statut

| Chantier | État |
|---|---|
| Cadrage du besoin et du périmètre | Rédigé, en attente de validation de l'encadrement |
| Définitions métier (demande, couverture, écart) | Rédigées (chapitre 3 du dossier) |
| Vérification des sources de données | Faite, quelques points à confirmer au premier appel d'API |
| Architecture et décisions (ADR) | 12 décisions rédigées au statut proposé, dont une acceptée provisoirement en local |
| Étude des embeddings (demande de l'encadrant) | En cours |
| Implémentation | Non démarrée |

Ce dépôt ne contient pas encore de code. Les sections ci-dessous décrivent l'**intention** et les choix connus, sans présenter comme réalisé ce qui ne l'est pas.

## Principes de conception

1. **Chaque indicateur est défini avant d'être calculé.** Pas de score sans formule écrite et testable.
2. **Tout est traçable.** Une compétence signalée renvoie aux offres et aux modules qui la justifient.
3. **Les sources sont remplaçables.** L'accès à une source peut être restreint ou retiré ; le système ne dépend d'aucune en particulier.
4. **Les traitements sont rejouables.** Une étape peut être relancée sans effet de bord.
5. **Les limites sont documentées.** Biais connus, données manquantes et hypothèses sont écrits, pas cachés.

## Architecture cible

Un monolithe modulaire (Spring Boot et Spring Modulith) de cinq modules, une base PostgreSQL, une application Angular. Décisions au statut proposé, voir le dossier de cadrage.

```mermaid
flowchart LR
    subgraph Sources
        A["La Bonne Alternance : offres"]
        B["ESCO v1.2.1"]
        C["France Travail : agrégats"]
        M["Maquette du M1 : import"]
    end
    subgraph App["Application Spring Boot : monolithe modulaire"]
        I[Ingestion]
        R["Référentiel"]
        P[Programmes]
        N[Analytique]
        X["Accès"]
    end
    A --> I
    C --> I
    B --> R
    M --> P
    I --> D[("PostgreSQL")]
    R --> D
    P --> D
    N --> D
    X --> D
    U["Navigateur : Angular"] -->|HTTPS| App
```

## Sources de données

Constat au 17 octobre 2026. Les points marqués *à vérifier* ne sont pas confirmés par la documentation officielle.

| Source | Contenu | Accès constaté | Réserves |
|---|---|---|---|
| La Bonne Alternance (offres) | Offres d'alternance avec leur texte, compétences déclarées, codes ROME, niveau de diplôme, dates | Clé gratuite, lecture sans habilitation. Recherche : 60 appels par minute. Export mis à jour chaque jour à 3 h : 2 appels par minute | Conditions d'utilisation : usage non lucratif, pas de commercialisation, pas de communication à des tiers, suspension possible sans préavis |
| La Bonne Alternance (certifications) | Blocs de compétences RNCP et codes ROME d'un diplôme | Même clé | Fiche RNCP 39490 (Master MIAGE) identifiée ; contenu de la route à vérifier |
| API Marché du travail (France Travail) | Offres par métier, secteur et compétence, trimestrielles | Ouvert | Authentification et licence à vérifier |
| API Offres d'emploi (France Travail) | Texte des offres | **Restreint**, sur demande | Non exposée publiquement le temps d'une analyse des conditions de réutilisation |
| ESCO v1.2.1 | Référentiel de compétences, 28 langues | Gratuit, réutilisable « pour tout usage » | Mention obligatoire, modifications à signaler |

Une clé de test dirige toutes les requêtes, lectures comprises, vers un environnement de test : les données réelles exigent une clé de production. Un quota dépassé renvoie le code 419 selon la spécification (à confirmer au premier appel). La clé d'API est secrète et ne doit jamais figurer dans le dépôt, pas plus que le texte des offres (conditions d'utilisation de la source).

### Mention ESCO

Ce projet utilise la classification ESCO de la Commission européenne.

## Stack

| Couche | Choix | État |
|---|---|---|
| Interface web | Angular 22, Apache ECharts, client TypeScript généré depuis le contrat OpenAPI | Proposé (ADR-004), remplace le choix initial de React |
| API | Java, Spring Boot, Spring Modulith, REST avec contrat OpenAPI généré du code | Proposé (ADR-003, ADR-012) |
| Collecte | Spring Batch, déclenchée après 3 h (Europe/Paris) | Proposé (ADR-009) |
| Base de données | PostgreSQL, migrations Flyway | Proposé (ADR-008) |
| Extraction des compétences | Méthode des termes en Java derrière une interface ; embeddings à l'étude | Proposé (ADR-010) |
| Identité et accès | Comptes locaux, Spring Security, session par cookie | Proposé (ADR-013) |
| Exécution | Docker Compose sur un serveur virtuel | Proposé (ADR-014) |
| Intégration continue | GitHub Actions | Proposé (ADR-017) |

Une seule décision est acceptée provisoirement, pour le mode local : la conservation des données brutes (ADR-011).

## Structure du dépôt

Structure cible, créée au fil de l'implémentation.

```
SkillPath_/
├── README.md
├── docs/            dossier de cadrage, ADR, architecture, étude des embeddings
├── backend/         application Spring Boot (cinq modules) et migrations Flyway
├── frontend/        application Angular
├── deploy/          Docker Compose
└── .github/         modèles d'issues et de pull requests, CI
```

## Documentation

- **Dossier de cadrage** : version 0.2, à versionner dans `docs/`.
- **Décisions d'architecture (ADR)** : 17 fiches rédigées dans l'annexe A du dossier, à extraire en un fichier par décision dans `docs/adr/`, avec les alternatives écartées.
- **Architecture** : schémas Mermaid dans `docs/architecture/`, après validation de l'encadrement.

## Démarrer

Aucun composant n'est exécutable pour l'instant. Cette section sera complétée avec l'objectif d'un démarrage en une commande dès le premier livrable.

## Fonctionnement de l'équipe

- La branche `main` sera protégée (protection de branche à activer) : toute modification passera par une pull request relue par un autre membre.
- Chaque tâche correspondra à une issue, suivie dans le GitHub Project « SkillPath Audit ».
- Les messages de commit suivent la convention Conventional Commits.
- Une décision structurante donne lieu à un ADR.

## Équipe

| Nom | Rôle |
|---|---|
| Abdellah JEBAR | À définir |
| Yao Junior Guy-Ange KRA | À définir |
| Abdelouarite GHILAN EL HASSANI | À définir |
| Maimouna BAH | À définir |
| Bouchra MEZRHAB | À définir |
| Elhadj Oumar DIALLO | À définir |

Encadrement : M. Donati, Master 1 MIAGE, Université Côte d'Azur.

## Licence

À définir. Le dépôt est privé et le projet est réalisé dans un cadre universitaire.
