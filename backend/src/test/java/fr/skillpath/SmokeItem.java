package fr.skillpath;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/** Agrégat de test (Spring Data JDBC) : sert seulement à valider la pile. */
@Table("smoke_item")
public record SmokeItem(@Id Long id, String label) {
}
