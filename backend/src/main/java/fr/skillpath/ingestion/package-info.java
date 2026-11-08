/**
 * Module Ingestion : récupération des offres par des adaptateurs de source, filtre du domaine,
 * déduplication, texte exploitable, brut filtré, journal des collectes.
 *
 * <p>Dépend du module Référentiel (domaine, codes ROME).
 */
@ApplicationModule(allowedDependencies = {"referentiel", "commun"})
package fr.skillpath.ingestion;

import org.springframework.modulith.ApplicationModule;
