# Vue d'ensemble de l'architecture

Schémas tirés du chapitre 5 du dossier de cadrage, version 0.2. Les décisions sont au statut proposé : voir [docs/adr/](../adr/README.md).

## Conteneurs et modules

Un monolithe modulaire de cinq modules, une base PostgreSQL, un serveur virtuel exécutant Docker Compose. Deux frontières de confiance : celle des utilisateurs, et celle des sources externes. Voir [ADR-003](../adr/adr-003.md) et [ADR-014](../adr/adr-014.md).

```mermaid
flowchart LR
    U["Navigateur : Angular 22 et ECharts"] -->|HTTPS| W["Serveur web : TLS, fichiers statiques"]
    subgraph SV["Serveur virtuel : Docker Compose"]
        W --> APP
        subgraph APP["Application Spring Boot : monolithe modulaire"]
            I[Ingestion]
            R["Référentiel"]
            P[Programmes]
            N[Analytique]
            X["Accès"]
        end
        APP --> D[("PostgreSQL")]
        F["Flyway : migrations"] --> D
    end
    APP -->|"API"| LBA["La Bonne Alternance"]
    APP -->|"fichier"| ESCO["ESCO v1.2.1"]
    APP -->|"alertes sans texte d'offre"| SMTP["Messagerie SMTP"]
    D -->|"sauvegardes"| SAV["Sauvegardes hors serveur"]
```

## Collecte de nuit

Un job Spring Batch par collecte, déclenché après 3 h (Europe/Paris). L'export est limité à deux appels par minute, et son adresse est valable deux minutes. Voir [ADR-009](../adr/adr-009.md).

```mermaid
sequenceDiagram
    participant B as Spring Batch
    participant L as La Bonne Alternance
    participant D as PostgreSQL
    participant M as Messagerie
    B->>L: appel 1 : export
    L-->>B: URL valable 2 min
    B->>L: appel 2 : téléchargement
    L-->>B: offres avec texte
    B->>D: filtre, stocke, déduplique, extrait, agrège
    B->>D: journal de la collecte
    B-)M: en cas d'échec : courriel sans texte d'offre
```

Chemins d'échec : quota dépassé (code 419 selon la spécification, à confirmer au premier appel) puis attente et nouvelle tentative ; adresse expirée puis deux nouveaux appels ; étape en erreur puis reprise avec les mêmes paramètres, sans doublon ; aucune collecte réussie depuis 26 heures : alerte.

## Consultation d'un indicateur

Le texte d'une offre n'est jamais renvoyé : seules les preuves (compétences extraites, titre, lien de candidature, modules) le sont. Voir [ADR-005](../adr/adr-005.md), [ADR-012](../adr/adr-012.md) et [ADR-013](../adr/adr-013.md).

```mermaid
sequenceDiagram
    participant N as Navigateur Angular
    participant A as API Spring Security
    participant D as PostgreSQL
    N->>A: consulte un segment
    A->>A: session valide ? rôle autorisé ?
    A->>D: lit demande, couverture, écart, intervalles
    D-->>A: résultats
    A-->>N: indicateurs, jamais de texte d'offre
    N->>A: ouvre une compétence
    A->>D: extractions, titre, lien, modules
    A-->>N: preuves sans texte complet
```

Codes d'erreur : 401 si non connecté, 403 si le rôle n'est pas autorisé, 503 si la base est indisponible.

## Cycle de vie des données

À partir de l'état « extrait », plus aucun texte d'offre : seulement des données dérivées. Voir [ADR-008](../adr/adr-008.md) et [ADR-011](../adr/adr-011.md).

```mermaid
flowchart LR
    S["Export de La Bonne Alternance"] --> B["1. Brut : export filtré"]
    B --> N["2. Normalisé : champs inutiles retirés, déduplication"]
    N --> E["3. Extrait : compétences, méthode, version"]
    E --> A["4. Agrégé : instantanés, intervalles"]
    classDef avecTexte fill:#fbece8,stroke:#C8553D
    classDef sansTexte fill:#e9edf3,stroke:#1F3A5F
    class B,N avecTexte
    class E,A sansTexte
```
