"""
Produit les migrations Flyway du module Référentiel (ADR-029) à partir des deux tables CSV.

Usage :  python generer_migration_sql.py <competence.csv> <alias_competence.csv> <dossier_sortie>

Écrit dans <dossier_sortie> :
  V1__creer_tables_competence_et_alias.sql   création des tables (schéma « referentiel »)
  V2__charger_competences_et_alias.sql       insertion des données (ESCO v1.2.1 FR + liste locale)

Une migration appliquée ne se modifie jamais (ADR-029) : si les données changent, relancer ce
script vers un NOUVEAU numéro (V3, V4...) avec seulement les changements. Aucune donnée d'offre.
"""
import csv
import sys
from pathlib import Path

V1 = """-- Module Référentiel (lot 6) : compétences (ESCO v1.2.1 FR et liste locale) et alias.
-- Schéma propre au module (ADR-029). Aucune clé étrangère vers un autre schéma (ADR-022).
CREATE SCHEMA IF NOT EXISTS referentiel;

CREATE TABLE referentiel.competence (
    competence_id       text PRIMARY KEY,
    source              text NOT NULL CHECK (source IN ('ESCO', 'liste_locale')),
    uri_esco            text,
    libelle             text NOT NULL,
    type                text,
    dans_ensemble_c     boolean NOT NULL,
    transversale        boolean NOT NULL,
    parent_esco         text,
    statut_liste_locale text
);

CREATE TABLE referentiel.alias_competence (
    alias_id       bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    competence_id  text NOT NULL REFERENCES referentiel.competence (competence_id),
    alias          text NOT NULL,
    langue         text NOT NULL CHECK (langue IN ('fr', 'en')),
    statut         text NOT NULL CHECK (statut IN ('retenu', 'ecarte')),
    provenance     text NOT NULL,
    UNIQUE (competence_id, alias, langue)
);

CREATE INDEX alias_competence_competence_idx ON referentiel.alias_competence (competence_id);
CREATE INDEX competence_source_idx ON referentiel.competence (source);
"""


def lit(v):
    return "NULL" if v is None or v == "" else "'" + v.replace("'", "''") + "'"


def booleen(v):
    return "true" if v == "oui" else "false"


def par_paquets(lignes, n=500):
    for i in range(0, len(lignes), n):
        yield lignes[i:i + n]


def main(comp, alias, sortie):
    out = Path(sortie)
    out.mkdir(parents=True, exist_ok=True)
    with open(comp, encoding="utf-8-sig", newline="") as f:
        c = list(csv.DictReader(f, delimiter=";"))
    with open(alias, encoding="utf-8-sig", newline="") as f:
        a = list(csv.DictReader(f, delimiter=";"))
    (out / "V1__creer_tables_competence_et_alias.sql").write_text(V1, encoding="utf-8", newline="\n")
    p = ["-- Données du module Référentiel : ESCO v1.2.1 FR et liste locale (généré, ne pas modifier).",
         "-- Source : tools/referentiel/generer_tables_referentiel.py puis generer_migration_sql.py.", ""]
    cols = "(competence_id, source, uri_esco, libelle, type, dans_ensemble_c, transversale, parent_esco, statut_liste_locale)"
    for lot in par_paquets(c):
        vals = ",\n".join("  (%s, %s, %s, %s, %s, %s, %s, %s, %s)" % (
            lit(r["competence_id"]), lit(r["source"]), lit(r["uri_esco"]), lit(r["libelle"]),
            lit(r["type"]), booleen(r["dans_ensemble_c"]), booleen(r["transversale"]),
            lit(r["parent_esco"]), lit(r["statut_liste_locale"])) for r in lot)
        p.append(f"INSERT INTO referentiel.competence {cols} VALUES\n{vals};\n")
    cols = "(competence_id, alias, langue, statut, provenance)"
    for lot in par_paquets(a):
        vals = ",\n".join("  (%s, %s, %s, %s, %s)" % (
            lit(r["competence_id"]), lit(r["alias"]), lit(r["langue"]), lit(r["statut"]),
            lit(r["provenance"])) for r in lot)
        p.append(f"INSERT INTO referentiel.alias_competence {cols} VALUES\n{vals};\n")
    (out / "V2__charger_competences_et_alias.sql").write_text("\n".join(p), encoding="utf-8", newline="\n")
    print(f"{len(c)} compétences, {len(a)} alias -> {out}")


if __name__ == "__main__":
    if len(sys.argv) != 4:
        sys.exit(__doc__)
    main(*sys.argv[1:])
