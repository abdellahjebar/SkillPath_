/**
 * Module Programmes : formations, versions (brouillon puis publié), unités d'enseignement,
 * modules (matières), niveaux de traitement des compétences.
 *
 * <p>Dépend des modules Référentiel et Extraction (appel « texte vers mentions »).
 */
@ApplicationModule(allowedDependencies = {"referentiel", "extraction", "commun"})
package fr.skillpath.programmes;

import org.springframework.modulith.ApplicationModule;
