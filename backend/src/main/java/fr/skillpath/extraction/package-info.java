/**
 * Module Extraction : détection des compétences dans un texte (dictionnaire, embeddings
 * conditionnels), mentions, jeu annoté et évaluation. Appelé pour les offres et pour les
 * descriptions de modules.
 *
 * <p>Dépend des modules Référentiel (compétences, alias) et Ingestion (lecture des offres).
 */
@ApplicationModule(allowedDependencies = {"referentiel", "ingestion", "commun"})
package fr.skillpath.extraction;

import org.springframework.modulith.ApplicationModule;
