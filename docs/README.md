# Documentation

| Dossier | Contenu | État |
|---|---|---|
| [adr/](adr/README.md) | Fiches de décision d'architecture, une par fichier (ADR 001 à 017, 020 à 027 et 028) | Décidées le 8 novembre 2026, sauf l'ADR 028 (23 novembre 2026) ; ADR 018 et 019 à venir après les mesures |
| [architecture/](architecture/vue-densemble.md) | Schémas Mermaid : conteneurs et sept modules, collecte, consultation, cycle de vie des données | Rédigé |
| [GUIDE-EQUIPE.md](GUIDE-EQUIPE.md) | Guide d'intégration : par où commencer | Rédigé |
| [repartition.md](repartition.md) | Les quinze lots et qui prend quoi | À compléter par l'équipe |
| [catalogue-des-lots.md](catalogue-des-lots.md) | Une fiche de départ par lot | Rédigé |
| [references.md](references.md) | Bibliographie numérotée, identique à celle du dossier | Rédigée |

À venir, selon l'avancement du projet :

- le dossier de cadrage en PDF (version 0.3) ;
- l'étude des embeddings demandée par l'encadrant ;
- le modèle de données et le document d'interfaces entre modules ;
- l'exploitation : procédure de restauration, conduite à tenir si la collecte échoue, rotation de la clé d'API ;
- la qualité : jeux de référence et rapport d'évaluation de l'extraction.

## Règles

- Un ADR par fichier, nommé `adr-NNN.md`.
- Toute affirmation renvoie à une source ou est marquée « à vérifier ».
- Les schémas sont écrits en Mermaid, pour être relus et versionnés comme du code.
