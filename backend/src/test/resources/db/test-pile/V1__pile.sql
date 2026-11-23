-- Migration de test : valide que Flyway s'exécute sur PostgreSQL 18.
-- Ces tables n'existent que pour les tests de la pile ; aucune table métier n'est créée ici.

CREATE TABLE smoke_item (
    id    BIGSERIAL PRIMARY KEY,
    label TEXT NOT NULL
);

-- Clé naturelle de l'ADR-027 : source, identifiant de la source, date de collecte.
CREATE TABLE smoke_offre (
    source        TEXT NOT NULL,
    id_source     TEXT NOT NULL,
    date_collecte DATE NOT NULL,
    texte         TEXT NOT NULL,
    PRIMARY KEY (source, id_source, date_collecte)
);
