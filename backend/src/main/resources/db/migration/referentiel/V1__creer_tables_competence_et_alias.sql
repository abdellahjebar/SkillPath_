-- Module Référentiel (lot 6) : compétences (ESCO v1.2.1 FR et liste locale) et alias.
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
