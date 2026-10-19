# Sécurité

Le dépôt est privé et le projet est réalisé dans un cadre universitaire. Ce document fixe les règles qui protègent les secrets et les données.

## Signaler un problème

Pour une vulnérabilité, une fuite de secret ou une exposition de données d'offres, prévenir **directement** le responsable du dépôt, Abdellah JEBAR, sans passer par une issue ou un message visible de tous.

## Secrets

- Les secrets (clé d'API de production de La Bonne Alternance, mots de passe, jetons, identifiants de messagerie) vivent dans des variables d'environnement, **jamais dans le dépôt**.
- Un fichier `.env.example` peut exister, avec les noms des variables et aucune valeur.
- La chaîne d'intégration continue ne contient aucune clé de production (ADR-017). Les actions tierces sont épinglées sur un identifiant de commit et le jeton est en lecture seule par défaut.

### Si un secret a été versionné

1. Le considérer comme **compromis**, même si le commit a été supprimé ensuite.
2. Révoquer ou régénérer immédiatement la clé auprès du fournisseur.
3. Prévenir le responsable du dépôt.
4. Nettoyer l'historique seulement après la rotation de la clé.

## Données

- Le **texte des offres** n'est jamais versionné, exposé par l'API, écrit dans un journal ni envoyé par courriel. Les conditions d'utilisation de la source interdisent de le communiquer à des tiers (ADR-005, ADR-011).
- Le téléphone du recruteur et l'identifiant du destinataire sont retirés avant stockage.
- Le `.gitignore` bloque les répertoires et fichiers de données d'offres. Ne pas contourner cette protection.

## Journaux et alertes

Aucun texte d'offre ni donnée personnelle dans les journaux ni dans les courriels d'alerte (ADR-015). Le point de santé public ne renvoie aucun détail ; le reste de l'Actuator est protégé.

## Dépendances

Dependabot surveille les actions GitHub. D'autres écosystèmes (Maven, npm) seront ajoutés avec le code.

## Versions prises en charge

Le projet n'a pas encore de version publiée.
