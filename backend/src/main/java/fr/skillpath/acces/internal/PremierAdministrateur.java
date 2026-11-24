package fr.skillpath.acces.internal;

import fr.skillpath.commun.Roles;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crée le premier administrateur au démarrage, s'il n'y en a aucun et si les variables d'environnement
 * {@code SKILLPATH_ACCES_ADMINISTRATEUR_IDENTIFIANT} et {@code SKILLPATH_ACCES_ADMINISTRATEUR_MOT_DE_PASSE}
 * sont définies. Le mot de passe n'est jamais écrit dans un fichier ni dans les journaux ; il doit respecter
 * la politique de longueur. Sans variables, rien n'est créé.
 */
@Component
class PremierAdministrateur implements ApplicationRunner {

    private static final Logger JOURNAL = LoggerFactory.getLogger(PremierAdministrateur.class);

    private final ComptesJdbc comptes;
    private final PasswordEncoder encodeur;
    private final ParametresAcces parametres;

    PremierAdministrateur(ComptesJdbc comptes, PasswordEncoder encodeur, ParametresAcces parametres) {
        this.comptes = comptes;
        this.encodeur = encodeur;
        this.parametres = parametres;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        var admin = parametres.administrateur();
        if (!admin.defini() || comptes.existeUnAdministrateur()) {
            return;
        }
        PolitiqueMotDePasse.verifier(admin.motDePasse());
        comptes.creer(admin.identifiant(), encodeur.encode(admin.motDePasse()), Set.of(Roles.ADMINISTRATEUR));
        JOURNAL.info("Premier administrateur créé.");
    }
}
