package fr.skillpath.acces.internal.web;

import fr.skillpath.acces.Utilisateur;
import fr.skillpath.acces.internal.UtilisateurConnecte;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Utilisateur courant. La connexion (POST) et la déconnexion (DELETE) du même chemin sont traitées par
 * la chaîne de sécurité. L'interface appelle ce chemin au démarrage et après chaque connexion ou
 * déconnexion : la réponse (ou le 401) écrit aussi le cookie {@code XSRF-TOKEN}.
 */
@RestController
@RequestMapping("/api/v1/session")
public class SessionController {

    @GetMapping
    Utilisateur utilisateurCourant(Authentication authentification) {
        return UtilisateurConnecte.depuis(authentification);
    }
}
