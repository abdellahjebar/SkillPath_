-- Comptes du module Accès (ADR-013). Les migrations de ce dossier s'exécutent dans le schéma « acces ».

CREATE TABLE utilisateur (
    id                    BIGSERIAL PRIMARY KEY,
    identifiant           TEXT        NOT NULL,
    mot_de_passe_hache    TEXT        NOT NULL,
    actif                 BOOLEAN     NOT NULL DEFAULT TRUE,
    -- Identifiant chez un fournisseur externe, prévu pour une connexion unique de l'établissement (ADR-013).
    identifiant_externe   TEXT,
    -- Limitation des tentatives par compte, pas par adresse (OWASP, Authentication Cheat Sheet).
    echecs_consecutifs    INTEGER     NOT NULL DEFAULT 0,
    premier_echec_le      TIMESTAMPTZ,
    verrouille_jusqua     TIMESTAMPTZ,
    cree_le               TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Un identifiant est unique sans tenir compte de la casse.
CREATE UNIQUE INDEX utilisateur_identifiant_unique ON utilisateur (lower(identifiant));
CREATE UNIQUE INDEX utilisateur_identifiant_externe_unique ON utilisateur (identifiant_externe)
    WHERE identifiant_externe IS NOT NULL;

CREATE TABLE utilisateur_role (
    utilisateur_id BIGINT NOT NULL REFERENCES utilisateur (id) ON DELETE CASCADE,
    role           TEXT   NOT NULL CHECK (role IN ('RESPONSABLE', 'MEMBRE_CONSEIL', 'ENSEIGNANT', 'ADMINISTRATEUR')),
    PRIMARY KEY (utilisateur_id, role)
);
