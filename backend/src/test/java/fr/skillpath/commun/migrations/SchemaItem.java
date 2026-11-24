package fr.skillpath.commun.migrations;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/** Agrégat de test d'un module fictif : la table est dans le schéma du module. */
@Table(schema = "alpha", value = "item")
public record SchemaItem(@Id Long id, String label) {
}
