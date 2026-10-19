# Contribuer à SkillPath Audit

Ce guide décrit comment l'équipe travaille sur le dépôt. Il applique les règles du dossier de cadrage (chapitres 8 et 10).

## Principes

- La branche `main` est la référence. Elle sera protégée : aucune modification directe, tout passe par une pull request relue par un autre membre.
- Les branches sont courtes et ciblées. Une branche, une tâche.
- Tout le code est relu par un autre membre avant de rejoindre `main`.

## Branches

Format : `type/numero-issue-description-courte`

| Type | Usage |
|---|---|
| `feat/` | nouvelle fonctionnalité |
| `fix/` | correction d'anomalie |
| `docs/` | documentation, ADR, schémas |
| `test/` | tests seuls |
| `chore/` | outillage, dépendances, configuration |

Exemple : `feat/12-telechargement-export`.

## Commits

Les messages suivent la convention Conventional Commits : `type(portée): résumé à l'impératif`.

```
feat(ingestion): télécharger l'export en deux appels
fix(analytique): corriger la borne haute de l'intervalle de Wilson
docs(adr): proposer l'ADR-018
```

Portées utiles : `ingestion`, `referentiel`, `programmes`, `analytique`, `acces`, `api`, `frontend`, `ci`, `docs`.

## Pull requests

1. Ouvrir la pull request depuis une branche liée à une issue.
2. Remplir le modèle fourni.
3. Attendre que l'intégration continue soit verte.
4. Obtenir au moins une relecture d'un autre membre, jamais de l'auteur.
5. Fusionner uniquement quand tous les commentaires sont traités.

Garder les pull requests petites : une relecture sérieuse est impossible au delà de quelques centaines de lignes.

## Définition de « terminé »

Une tâche est terminée lorsque :

- le code est relu par un autre membre ;
- les tests passent, dont les tests de conformité ;
- la documentation concernée et la fiche de décision sont à jour ;
- le dossier compile ;
- une démonstration est possible.

## Tests

La stratégie suit la pyramide de tests de l'[ADR-016](docs/adr/adr-016.md) : beaucoup de tests unitaires, des tests d'intégration avec un PostgreSQL réel en conteneur, la vérification de la structure des modules, des tests de conformité, deux ou trois parcours de bout en bout.

Tests de conformité obligatoires à chaque modification :

- le contrat de l'API ne contient aucun champ de texte complet d'offre ;
- les droits par rôle sont respectés ;
- la structure des modules est valide (pas de cycle, accès par l'interface publique).

## Décisions d'architecture

Toute décision structurante donne lieu à un ADR. Format et index : [docs/adr/](docs/adr/README.md).

1. Ouvrir une issue de type « décision d'architecture ».
2. Écrire la fiche : contexte, options, critères et poids fixés avant la notation, décision proposée, conséquences, ce qui la rouvrirait.
3. Statut initial : `Proposé`. Il passe à `Accepté` après notation et discussion de l'équipe, puis validation de l'encadrement.

## Règles de conformité

Ces règles viennent des conditions d'utilisation de la source des offres et du RGPD (ADR-005, ADR-011, ADR-015).

- Le texte d'une offre n'apparaît jamais dans l'API, les journaux, les courriels, les commits ni les captures d'écran partagées.
- Le téléphone du recruteur et l'identifiant du destinataire sont retirés avant tout stockage.
- Aucune clé, aucun mot de passe, aucun jeton dans le dépôt. Voir [SECURITY.md](SECURITY.md).
- Les fichiers d'exemples ne contiennent que des données fictives.

## Environnement de développement

Aucun composant n'est exécutable pour l'instant. Cette section sera complétée avec l'objectif d'un démarrage en une commande, par un seul fichier Docker Compose, dès le premier livrable.
