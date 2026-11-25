package fr.skillpath.extraction.internal;

/** Un alias tel qu'il figure dans le CSV ; langue, statut et provenance restent inchangés. */
public record AliasCharge(String libelle, String langue, String statut, String provenance) {
}
