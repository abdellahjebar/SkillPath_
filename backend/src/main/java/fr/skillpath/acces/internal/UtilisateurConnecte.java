package fr.skillpath.acces.internal;

import fr.skillpath.acces.Utilisateur;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/** Traduit l'authentification de Spring Security en {@link Utilisateur} (rôles sans le préfixe « ROLE_ »). */
public final class UtilisateurConnecte {

    private static final String PREFIXE = "ROLE_";

    private UtilisateurConnecte() {
    }

    public static Utilisateur depuis(Authentication authentification) {
        return new Utilisateur(authentification.getName(), authentification.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith(PREFIXE) ? role.substring(PREFIXE.length()) : role)
                .sorted()
                .toList());
    }
}
