package fr.skillpath.acces.internal;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Réglages du module Accès (préfixe {@code skillpath.acces}).
 *
 * @param tentativesMax nombre d'échecs de connexion qui verrouillent le compte
 * @param fenetre       durée pendant laquelle les échecs sont comptés
 * @param verrouillage  durée du verrouillage
 * @param administrateur compte du premier administrateur, créé au démarrage s'il n'y a aucun administrateur
 */
@ConfigurationProperties("skillpath.acces")
record ParametresAcces(
        int tentativesMax,
        Duration fenetre,
        Duration verrouillage,
        Administrateur administrateur) {

    ParametresAcces {
        tentativesMax = tentativesMax == 0 ? 5 : tentativesMax;
        fenetre = fenetre == null ? Duration.ofMinutes(15) : fenetre;
        verrouillage = verrouillage == null ? Duration.ofMinutes(15) : verrouillage;
        administrateur = administrateur == null ? new Administrateur(null, null) : administrateur;
    }

    /** Le mot de passe vient d'une variable d'environnement, jamais d'un fichier du dépôt. */
    record Administrateur(String identifiant, String motDePasse) {

        boolean defini() {
            return identifiant != null && !identifiant.isBlank() && motDePasse != null && !motDePasse.isBlank();
        }
    }
}
