# Référentiel : liste locale (lot 6, ADR-007)

`liste_locale.csv` : liste locale de compétences, complément d'ESCO v1.2.1 (« complément local, hors ESCO »).
Séparateur `;`, encodage UTF-8. La version est portée par la colonne `version` (actuellement `v0.2`).

| Colonne | Sens |
|---|---|
| `type` | `concept` (ex. déploiement par conteneurs) ou `outil` (ex. Docker) |
| `statut` | `propose` : absent d'ESCO, en attente de validation par M. Donati et les enseignants ; `partiel_esco` : ESCO ne le connaît que comme concept plus large ou synonyme ; `dans_esco` : présent dans ESCO (la ligne pointe vers l'URI) |
| `parent_esco`, `id_externe` | libellé et URI ESCO quand ils existent, sinon vides (aucune URI n'est inventée) |
| `provenance` | origine de l'entrée et résultat de la vérification dans ESCO v1.2.1 FR (22 novembre 2026) |

## Ce que dit cette version
- Vérification faite sur `skills_fr.csv` d'ESCO v1.2.1 FR (13 960 compétences).
- Angular et Jenkins existent dans ESCO (`dans_esco`) ; le dossier v0.3 (3.1) cite Angular parmi les termes absents : à corriger.
- Git, Excel et ETL ne sont trouvés qu'en concept plus large ou en synonyme (`partiel_esco`).
- Les autres entrées sont absentes d'ESCO (`propose`).

## Limites
- Proposition de départ : le seuil de fréquence de l'ADR-007 (au moins quatre offres et 3 % des offres du domaine) n'est **pas encore appliqué** ; certaines entrées viennent d'un petit échantillon d'offres.
- Aucune validation par M. Donati ni les enseignants à ce stade.
- Ne contient aucun texte d'offre.

Utilise la classification ESCO (Commission européenne), réutilisation libre avec mention : https://esco.ec.europa.eu
