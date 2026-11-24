"""
Génère les deux tables du module Référentiel à partir d'ESCO v1.2.1 FR et de la liste locale.

  competence.csv        une ligne par compétence (ESCO + liste locale)
  alias_competence.csv  une ligne par alias : libellé, langue (fr/en), statut

Usage :  python generer_tables_referentiel.py <dossier_esco> <liste_locale.csv> <dossier_sortie>
  <dossier_esco> : dossier avec skills_fr.csv, digitalSkillsCollection_fr.csv,
                   transversalSkillsCollection_fr.csv, broaderRelationsSkillPillar_fr.csv
Sortie : CSV UTF-8 sans BOM, séparateur « ; ». Aucune donnée d'offre.
Quand ESCO en anglais sera ajouté, ajouter ses libellés en alias langue « en » (à faire).
"""
import csv
import sys
from pathlib import Path


def lire(chemin, delim=","):
    with open(chemin, encoding="utf-8-sig", newline="") as f:
        return list(csv.DictReader(f, delimiter=delim))


def main(dossier_esco, liste_locale, sortie):
    e, loc, out = Path(dossier_esco), Path(liste_locale), Path(sortie)
    out.mkdir(parents=True, exist_ok=True)
    # skills_fr.csv contient 13 960 lignes mais 13 939 compétences : 21 URI sont présentes deux
    # fois (deux dates de modification). On garde la ligne la plus récente.
    derniere = {}
    for r in lire(e / "skills_fr.csv"):
        u = r["conceptUri"]
        if u not in derniere or r["modifiedDate"] > derniere[u]["modifiedDate"]:
            derniere[u] = r
    skills = list(derniere.values())
    numerique = {r["conceptUri"] for r in lire(e / "digitalSkillsCollection_fr.csv")}
    transversal = {r["conceptUri"] for r in lire(e / "transversalSkillsCollection_fr.csv")}
    locale = lire(loc, ";")

    # Les entrées locales déjà dans ESCO (statut dans_esco) ne créent pas de doublon :
    # elles ajoutent seulement leurs alias à la compétence ESCO correspondante.
    uri_vers_id = {}
    comp, alias = [], []
    for r in skills:
        uri = r["conceptUri"]
        cid = "esco:" + uri.rsplit("/", 1)[-1]
        uri_vers_id[uri] = cid
        comp.append([cid, "ESCO", uri, r["preferredLabel"], r["skillType"],
                     "oui" if uri in numerique else "non",
                     "oui" if uri in transversal else "non", "", ""])
        for a in (r["altLabels"] or "").split("\n"):
            if a.strip():
                alias.append([cid, a.strip(), "fr", "retenu", "ESCO"])

    for r in locale:
        if r["statut"] == "dans_esco" and r["id_externe"] in uri_vers_id:
            cid = uri_vers_id[r["id_externe"]]
            # compétence ESCO utilisée par la liste locale : comptée dans l'ensemble C
            for ligne in comp:
                if ligne[0] == cid:
                    ligne[5] = "oui"
        else:
            cid = r["id_local"]
            comp.append([cid, "liste_locale", r["id_externe"], r["libelle"], r["type"],
                         "oui", "non", r["parent_esco"], r["statut"]])
        for a in filter(None, r["alias_fr"].split(";")):
            alias.append([cid, a.strip(), "fr", "retenu", "liste_locale"])
        for a in filter(None, r["alias_en"].split(";")):
            alias.append([cid, a.strip(), "en", "retenu", "liste_locale"])

    with open(out / "competence.csv", "w", encoding="utf-8", newline="") as f:
        w = csv.writer(f, delimiter=";")
        w.writerow(["competence_id", "source", "uri_esco", "libelle", "type",
                    "dans_ensemble_c", "transversale", "parent_esco", "statut_liste_locale"])
        w.writerows(comp)
    with open(out / "alias_competence.csv", "w", encoding="utf-8", newline="") as f:
        w = csv.writer(f, delimiter=";")
        w.writerow(["competence_id", "alias", "langue", "statut", "provenance"])
        w.writerows(alias)
    print(f"{len(comp)} compétences, {len(alias)} alias -> {out}")


if __name__ == "__main__":
    if len(sys.argv) != 4:
        sys.exit(__doc__)
    main(*sys.argv[1:])
