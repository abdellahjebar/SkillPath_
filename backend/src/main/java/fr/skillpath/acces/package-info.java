/**
 * Module Accès : comptes et rôles. La protection des contrôleurs des autres modules passe par
 * la sécurité commune, pas par un appel à ce module.
 *
 * <p>Ne dépend d'aucun autre module (hors noyau commun).
 */
@ApplicationModule(allowedDependencies = {"commun"})
package fr.skillpath.acces;

import org.springframework.modulith.ApplicationModule;
