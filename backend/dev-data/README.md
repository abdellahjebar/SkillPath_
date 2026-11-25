# Donnees de developpement

Ce dossier contient des jeux d'exemple fictifs. Ils ne doivent jamais etre remplaces par des
exports d'offres, des textes d'offres ou des donnees personnelles.

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

La graine produit 100 offres entierement fictives et sans texte. Le segment est national (M18,
octobre 2026) et simule des offres deja dedoublonnees et admissibles au calcul de la demande.
La date de collecte simulee est le 5 novembre 2026. Aucune donnee de La Bonne Alternance n'est
utilisee : ce jeu illustre le format et les calculs, il ne decrit pas le marche reel.

Les offres contiennent uniquement des identifiants synthetiques et des identifiants de competences.
Les trois competences utilisent les identifiants canoniques du fichier
[`competence.csv`](../src/main/resources/referentiel/competence.csv) :

| Competence | Offres qui la mentionnent | Demande D |
|---|---:|---:|
| Java | 8 sur 100 | 8 % |
| SQL | 20 sur 100 | 20 % |
| PostgreSQL | 12 sur 100 | 12 % |

La formation et ses deux versions sont inventees. Le jeu reprend seulement les champs utiles a
l'exemple : UE, ECTS, ECUE, volumes CM/TD, coefficient et liens aux competences. Les identifiants
stables des ECUE se retrouvent dans le brouillon suivant. Aucun nom de professeur, descriptif de
cours, objectif, methode ou reference bibliographique n'est inclus.

Selon la formule de l'ADR-024, sur la version publiee de demonstration, la couverture K est le
niveau maximal divise par 3 et l'ecart G vaut `D * (1 - K)` :

| Competence | Niveau maximal | Couverture K | Ecart G |
|---|---:|---:|---:|
| Java | 2 | 2/3 | 2,67 % |
| SQL | 3 | 1 | 0 % |
| PostgreSQL | 1 | 1/3 | 8 % |

Ces nombres servent a expliquer et tester le calcul; ils ne constituent pas des resultats sur le
M1 ni sur les offres reelles. Le format JSON reste un prototype, a faire valider avec les lots 6,
7, 8 et 10.

## Ce qui reste a integrer

Le jeu n'est pas encore charge dans PostgreSQL. La base locale et les migrations par module sont
maintenant disponibles, mais aucune migration metier pour `programmes` n'existe encore et le
chargement du referentiel CSV en base reste a faire. Le chargeur de donnees de demonstration devra
etre separe des migrations de schema et ne s'executer qu'avec le profil `dev`.
