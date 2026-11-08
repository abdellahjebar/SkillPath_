/**
 * Noyau commun : configuration, gestion des erreurs, journalisation, journal d'audit,
 * types partagés (identifiants, dates, constantes de rôles). Aucune logique métier.
 *
 * <p>Règle : une chose n'entre ici que si au moins deux modules s'en servent.
 */
@ApplicationModule(type = ApplicationModule.Type.OPEN)
package fr.skillpath.commun;

import org.springframework.modulith.ApplicationModule;
