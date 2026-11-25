# Donnees de developpement

Ce dossier contient un corpus entierement synthetique pour illustrer le parcours des lots 6, 7,
10 et 13. Il ne doit jamais etre remplace par des exports d'offres, des textes d'offres ou des
donnees personnelles.

## Jeu synthetique de demonstration

Depuis `backend`, avec Java 25 :

```bash
./mvnw -B -Dtest=SyntheticDatasetGeneratorTest test
./mvnw -B -DskipTests test-compile \
  -Dexec.classpathScope=test \
  -Dexec.mainClass=fr.skillpath.devdata.SyntheticDatasetGenerator \
  '-Dexec.args=--seed 20261124 --output dev-data/examples/jeu-synthetique.json' \
  org.codehaus.mojo:exec-maven-plugin:3.5.0:java
```

Le generateur est un outil de test, pas un composant du serveur. La graine fixe produit 102
enregistrements fictifs : 100 offres admissibles et dedoublonnees pour le segment, une candidate
sous le seuil de 200 caracteres et un doublon exclu. `offerCount` vaut donc 100. Les longueurs
sont des valeurs de scenario; aucun texte source n'est conserve. La periode simulee est octobre
2026, pour le segment national M18, avec une date de collecte fictive au 5 novembre 2026. Aucun
export d'offres reelles ni aucune donnee de La Bonne Alternance n'est utilise.

Chaque enregistrement comporte un identifiant synthetique, la longueur de texte simulee, les
indicateurs d'admissibilite et de dedoublonnage, puis `expectedMentions`. Ces annotations de reference
contiennent l'identifiant de competence et le type de correspondance (`LIBELLE_EXACT` ou `ALIAS`).
Elles couvrent des alias retenus dans le CSV du Referentiel ainsi que des occurrences repetees. Le
doublon garde les memes annotations que sa cible, mais est exclu du calcul; la candidate sous le
seuil n'a aucune annotation.
Conformement aux exigences du depot, aucun texte d'offre, passage ni position dans le texte n'est
inclus dans le JSON.

`expectedMentions` est une annotation de reference pour illustrer les calculs, pas le resultat d'un
moteur d'extraction. L'interface `ExtractionApi` et le chargeur CSV temporaire sont maintenant dans
le projet, mais le jeu n'appelle pas encore un extracteur reel et ne permet pas de verifier les
positions de ses detections.

Les competences emploient des identifiants canoniques du fichier
[`competence.csv`](../src/main/resources/referentiel/competence.csv) :

| Competence | Offres distinctes admissibles qui la mentionnent | Demande D |
|---|---:|---:|
| Java | 8 sur 100 | 8 % |
| SQL | 20 sur 100 | 20 % |
| PostgreSQL | 12 sur 100 | 12 % |

La demande compte les offres distinctes, pas le nombre total de passages. Ainsi, plusieurs mentions
de SQL dans une meme offre ne font compter cette offre qu'une seule fois.

## Programme fictif et exemple analytique

La formation et ses deux versions sont inventees. Le jeu reprend uniquement les donnees utiles au
scenario : UE, ECTS, ECUE, volumes CM/TD, coefficient, identifiants stables, liens aux competences
et niveaux. Les identifiants des ECUE restent stables entre la version publiee et le brouillon.
Aucun nom de professeur, contenu de cours, objectif, methode ou reference bibliographique n'est
inclus. Ce programme ne represente pas le M1 reel.

Selon la formule de l'ADR-024, sur la version publiee de demonstration, la couverture K est le
niveau maximal divise par 3 et l'ecart G vaut `D * (1 - K)` :

| Competence | Niveau maximal | Couverture K | Ecart G |
|---|---:|---:|---:|
| Java | 2 | 2/3 | 2,67 % |
| SQL | 3 | 1 | 0 % |
| PostgreSQL | 1 | 1/3 | 8 % |

Ces nombres servent a expliquer et tester le calcul; ils ne constituent pas des resultats sur le
M1 ni sur les offres reelles.

## Limites et suite

Le corpus JSON n'est pas charge dans PostgreSQL et ne definit pas un schema metier. Le chargeur CSV
temporaire du module Extraction ne lit que les competences et leurs alias en memoire; il n'ecrit pas
en base. Le chargement de donnees de demonstration devra rester separe des migrations et etre
reserve au profil `dev`, apres validation des contrats et schemas des lots concernes.
