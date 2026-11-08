# Contribuer à SkillPath Audit

Ce guide décrit comment l'équipe travaille sur le dépôt. Il applique les règles du dossier de cadrage (chapitres 8 et 10).

## Principes

- La branche `main` est la référence. Aucune modification directe : tout passe par une pull request relue par un autre membre avant fusion.
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

Les titres suivent la convention Conventional Commits : `type(portée): résumé à l'impératif`, en anglais. Les descriptions de pull request sont en français.

```
feat(ingestion): download the export in two calls
fix(analytique): correct the upper bound of the Wilson interval
docs(adr): add ADR-028
```

Portées utiles : `ingestion`, `referentiel`, `extraction`, `programmes`, `analytique`, `acces`, `pipeline`, `commun`, `api`, `frontend`, `ci`, `docs`.

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
3. Statut initial : `Proposé`. Il passe à `Décidé le <date>` après discussion de l'équipe. Les structures du squelette sont un point de départ : toute évolution passe par une fiche de ce type.

## Règles de conformité

Ces règles viennent des conditions d'utilisation de la source des offres et du RGPD (ADR-005, ADR-011, ADR-015).

- Le texte d'une offre n'apparaît par défaut ni dans l'API, ni dans les journaux, les courriels, les commits ou les captures d'écran partagées. Toute sortie vers un service externe est explicite et journalisée (ADR-005, ADR-015).
- Le téléphone du recruteur et l'identifiant du destinataire sont retirés avant tout stockage ; les courriels et numéros présents dans le texte sont masqués à l'ingestion (ADR-011).
- Aucune clé, aucun mot de passe, aucun jeton dans le dépôt. Voir [SECURITY.md](SECURITY.md).
- Les fichiers d'exemples ne contiennent que des données fictives.

## Environnement de développement

Prérequis : Docker. Depuis le dossier `backend`, sans rien installer d'autre :

```
docker run --rm -v "${PWD}:/work" -w /work maven:3.9-eclipse-temurin-25 mvn -B verify
```

Le test `ModularityTests` liste les modules détectés et échoue si une frontière est franchie. Le démarrage complet en une commande (base de données, serveur, interface) viendra avec le lot Plateforme. Voir [backend/README.md](backend/README.md).
