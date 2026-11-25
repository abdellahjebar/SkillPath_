package fr.skillpath.extraction.internal;

import java.util.List;

/**
 * Fournit les données de compétences au futur moteur d'extraction.
 *
 * <p>Le chargeur CSV temporaire pourra être remplacé par un adaptateur du Référentiel sans modifier
 * les règles de détection. Cette interface reste interne au module Extraction.
 */
public interface SourceCompetences {

    /** Charge les compétences et leurs alias, sans modifier les données d'origine. */
    List<CompetenceChargee> charger();
}
