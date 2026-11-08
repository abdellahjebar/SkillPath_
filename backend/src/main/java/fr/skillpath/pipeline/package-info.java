/**
 * Module Pipeline : travail unique de nuit, au sommet. Appelle Ingestion, Extraction puis
 * Analytique dans l'ordre ; reprise sur échec, deux alertes.
 *
 * <p>Personne ne dépend de ce module.
 */
@ApplicationModule(allowedDependencies = {"ingestion", "extraction", "analytique", "commun"})
package fr.skillpath.pipeline;

import org.springframework.modulith.ApplicationModule;
