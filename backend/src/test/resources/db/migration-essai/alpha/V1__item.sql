-- Migration de test du module fictif « alpha » : sert à vérifier un schéma par module avec Spring Data JDBC.
CREATE TABLE item (
    id    BIGSERIAL PRIMARY KEY,
    label TEXT NOT NULL
);
