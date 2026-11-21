# Contrat de l'API

Le fichier [skillpath-api.yaml](skillpath-api.yaml) décrit l'API de la première version : UC1 (écarts d'un segment) et UC2 (une compétence et ses preuves). C'est la référence commune du serveur et de l'interface.

## Règles

- Aucun champ ne renvoie le texte complet d'une offre ([ADR-005](../../../../../docs/adr/adr-005.md)).
- Les erreurs suivent le format RFC 9457.
- Ajouter un champ ou un chemin est compatible. Retirer ou renommer un champ demande une nouvelle version du contrat et l'accord de l'équipe de l'interface.
- Les exemples sont fictifs. Ils servent au serveur factice.
- Pour proposer un changement : une pull request qui modifie ce fichier, relue par le responsable du lot Interface.

## Lancer le serveur factice

Prérequis : Docker. Depuis le dossier `backend` :

```
docker run --rm -p 4010:4010 -v "${PWD}/src/main/resources/openapi:/spec:ro" stoplight/prism:5.15.10 mock -h 0.0.0.0 -m false /spec/skillpath-api.yaml
```

Le serveur répond sur `http://localhost:4010`. Le drapeau `-m false` évite un plantage au démarrage constaté avec cette image.

Comme le vrai serveur, il exige une session : ajouter l'en-tête `Cookie: SESSION=test`, sinon la réponse est 401. Le navigateur envoie le cookie tout seul une fois créé dans les outils de développement.

Exemples d'essai :

```
curl -H "Cookie: SESSION=test" "http://localhost:4010/api/v1/ecarts?periode=2026-10"
curl -H "Cookie: SESSION=test" "http://localhost:4010/api/v1/competences/22222222-2222-4222-8222-222222222222?periode=2026-10"
```

Choisir une réponse précise avec l'en-tête `Prefer` :

| En-tête | Effet |
|---|---|
| `Prefer: example=segmentTropPetit` | Segment avec trop peu d'offres, liste vide (écarts) |
| `Prefer: code=404` | Compétence introuvable |
| `Prefer: code=503` | Service indisponible |

## Générer le client TypeScript

Le client Angular n'est pas committé : chacun le génère à partir du contrat, ce qui garantit qu'il ne s'écarte jamais de lui. Outil : [ng-openapi-gen](https://github.com/cyclosproject/ng-openapi-gen), qui génère des services Angular typés.

Dans le projet Angular (le dossier `frontend/` appartient au lot Interface, qui choisit l'emplacement du client), ajouter l'outil et un script :

```
npm install --save-dev ng-openapi-gen@1.1.0
```

```
"scripts": {
  "api:generer": "ng-openapi-gen --input ../backend/src/main/resources/openapi/skillpath-api.yaml --output src/app/api"
}
```

Puis `npm run api:generer`. Le dossier généré est à ignorer dans Git (fichier `.gitignore` du projet Angular) ; le fichier `skillpath-api.yaml` est la référence.

Vérifié le 21 novembre 2026 : avec ng-openapi-gen 1.1.0, le contrat actuel produit 13 modèles et 2 services (écarts et compétences), qui compilent en mode strict avec Angular 22.3.0 et TypeScript. Le chemin de sortie et la structure du dossier sont libres.

Remarques pour l'interface :

- La session est un cookie : en développement, passer par le proxy du serveur de développement d'Angular pour que le navigateur envoie le cookie sans configuration de CORS. Le client généré ne gère pas le cookie lui-même.
- Si le contrat change, relancer `npm run api:generer`. Un champ ajouté apparaît ; un champ retiré fait échouer la compilation là où l'interface l'utilise, ce qui signale le changement.

## Reste à faire dans le lot 2

- Contrôles de la CI : validation du contrat, régénération du client, absence de texte complet, changements cassants.
- Quand le serveur existera : contrat produit par le code (springdoc) avec test de dérive par rapport à ce fichier ([ADR-012](../../../../../docs/adr/adr-012.md)).
