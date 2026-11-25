package fr.skillpath.extraction.internal;

import java.util.List;

/**
 * Données nécessaires à la préparation du dictionnaire : libellé préféré et alias associés.
 *
 * <p>La source et le type sont conservés tels quels : un type ESCO et un type local n'ont pas
 * nécessairement la même signification. Aucune normalisation n'est appliquée au vocabulaire.
 */
public record CompetenceChargee(
        String competenceId,
        String libelle,
        String source,
        String type,
        List<AliasCharge> alias) {

    public CompetenceChargee {
        alias = List.copyOf(alias);
    }
}
