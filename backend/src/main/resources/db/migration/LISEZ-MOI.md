# Migrations Flyway

Règles de l'ADR-029. Un schéma PostgreSQL et un Flyway par module : le dossier `<module>/` de ce
répertoire contient les migrations du module et son nom est celui du schéma.

- Fichier : `db/migration/<module>/V<n>__<description>.sql`, avec `n` entier, propre au module
  (`V1__creer_competence.sql`). Le nom du dossier est en minuscules, chiffres et `_`.
- Une migration déjà appliquée ne se modifie jamais : on en ajoute une nouvelle.
- Toute migration passe par une pull request.
- Deux personnes d'un même module qui prennent le même numéro : la CI échoue, le second renumérote avant la fusion.
- Les tables restent dans le schéma du module, sans préfixe dans le SQL de la migration (le schéma du module
  est le schéma par défaut de ses migrations). Aucune clé étrangère ni jointure vers un autre schéma.
- Agrégats Spring Data JDBC : `@Table(schema = "<module>", value = "<table>")`. SQL explicite : tables qualifiées
  (`referentiel.competence`).
- Une migration refusée en local parce qu'une version plus basse arrive après une plus haute : repartir de zéro
  (`docker compose down -v`) ou renuméroter.

Aucune migration métier n'existe encore.
