# ESCO v1.2.1 FR : fichiers versionnés, source et chargement (lot 6, ADR-002)

Cette notice décrit **où trouver ESCO et comment l'utiliser**. Ce dossier contient **6 fichiers d'ESCO** (environ 13,5 Mo), copiés tels quels depuis le téléchargement officiel. Les 12 autres fichiers du zip (environ 32 Mo, dont `occupationSkillRelations_fr.csv` de 28 Mo) ne sont **pas** versionnés : ils ne servent pas au projet. ESCO est un jeu de données public, téléchargeable gratuitement.

## Source
- Classification européenne des compétences, métiers et qualifications (ESCO), Commission européenne : https://esco.ec.europa.eu
- Version utilisée : **v1.2.1, classification, français, format CSV** (page de téléchargement : https://esco.ec.europa.eu/en/use-esco/download, formulaire à remplir).
- Réutilisation libre, avec **mention obligatoire de l'utilisation d'ESCO** (ADR-002).

## Fichiers utilisés par le projet
| Fichier | Contenu | Lignes (données) |
|---|---|---|
| `skills_fr.csv` | toutes les compétences (libellé, synonymes, description, URI) | 13 960 |
| `digitalSkillsCollection_fr.csv` | sous-ensemble des compétences numériques | 1 284 |
| `skillsHierarchy_fr.csv` | arborescence des compétences | 640 |
| `skillGroups_fr.csv` | groupes de compétences | 640 |
| `broaderRelationsSkillPillar_fr.csv` | liens parent/enfant de toutes les compétences (nécessaire pour le parent ESCO d'une compétence hors numérique) | 20 819 |
| `transversalSkillsCollection_fr.csv` | compétences transversales (stockées, exclues du calcul de l'écart, ADR-006) | 95 |

Les autres fichiers du zip (métiers, vert, recherche, langues, ISCO, dictionnaire) ne servent pas au projet pour l'instant.

## Vérifier qu'on a la bonne version (SHA-256)
| Fichier | SHA-256 |
|---|---|
| `skills_fr.csv` | `32be602f7ab09084b418dfc91ace621383235592e7a777e19ad3b3c7b5dad026` |
| `digitalSkillsCollection_fr.csv` | `f5f4e0874f632705267157ac83690d1a9e125e9687eacf52541a3fb76c7f0215` |
| `skillsHierarchy_fr.csv` | `549c847098ebc15766575ff06f2dec433e74360ec74de3b2d83c31a2b609b0e5` |
| `skillGroups_fr.csv` | `d2f9616da23e5b39448933d55699e847253730f4e486681fa85a676079f5204f` |
| `broaderRelationsSkillPillar_fr.csv` | `56b70f4852b1f53c6192c979310c34c93f95436621d907083cf0d55b51d8e42d` |
| `transversalSkillsCollection_fr.csv` | `5b9ffe87e15ad502fb7770a53700dd1bfe2f9c43049613dd42faf2846e273951` |

Windows (PowerShell) : `Get-FileHash .\skills_fr.csv -Algorithm SHA256`

## Ce qu'on a constaté (22 novembre 2026, sur `skills_fr.csv`)
- Présents : Python, SQL, Java, JavaScript, TypeScript, PHP, C#, CSS, DevOps, Ansible, PostgreSQL, MySQL, **Angular**, **Jenkins**.
- Absents : Docker, Kubernetes, Excel, Power BI, Terraform, Spring.
- Seulement en synonyme ou en concept plus large : Git, Scrum, ETL ; Linux n'apparaît que sous « Kali Linux ».
- Ces constats alimentent `liste_locale.csv`, qui complète ESCO pour les termes absents (ADR-007).

## Choix de versionnement
- Aucune compétence n'est perdue : `skills_fr.csv` contient les 13 960 compétences avec leur libellé et leur description (les synonymes n'existent que pour 1 933 d'entre elles, dans ce même fichier).
- Fichiers non versionnés : métiers (`occupations_fr.csv`, `occupationSkillRelations_fr.csv`, `broaderRelationsOccPillar_fr.csv`), `skillSkillRelations_fr.csv`, ISCO, collections vert, recherche, langues, DigComp, `greenShareOcc_fr.csv` et `dictionary_fr.csv`. À ajouter si un lot en a besoin.
- Seuls les 4 fichiers utiles sont dans le dépôt (la version complète pèse 46 Mo). Une nouvelle version d'ESCO ajoutera une nouvelle copie dans l'historique Git : à ne faire que si nécessaire.
- Le fichier `.gitattributes` de ce dossier garde les fichiers octet pour octet (sans lui, le `.gitattributes` du dépôt convertirait les fins de ligne et les SHA-256 ci-dessus ne correspondraient plus).
- Ne jamais placer de données d'offres dans un dossier `data/`, `exports/` ou `offres/` : la CI les refuse.

## Chargement dans le serveur : **pas encore fait**
Le chargement d'ESCO dans le module Référentiel (lecture des CSV, versionnement, alias français et anglais) reste à écrire : c'est le cœur du lot 6 (issue #9). Les lots 8 et 5 en dépendent pour détecter les compétences. À décider avec l'équipe : comment le serveur trouve les fichiers (par exemple une variable d'environnement qui donne le dossier).

## Mention d'utilisation d'ESCO
Ce projet utilise la classification ESCO v1.2.1 (Commission européenne).
