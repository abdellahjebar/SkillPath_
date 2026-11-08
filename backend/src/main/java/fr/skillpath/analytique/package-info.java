/**
 * Module Analytique : demande, couverture, écart, instantanés, volume de collecte, export CSV.
 * Les formules sont en Java pur, testées sur des valeurs de référence.
 *
 * <p>Lit Ingestion, Extraction, Référentiel et Programmes ; aucun module métier ne dépend de lui.
 */
@ApplicationModule(allowedDependencies = {"ingestion", "extraction", "referentiel", "programmes", "commun"})
package fr.skillpath.analytique;

import org.springframework.modulith.ApplicationModule;
