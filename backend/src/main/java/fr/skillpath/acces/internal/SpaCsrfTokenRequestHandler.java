package fr.skillpath.acces.internal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.Supplier;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

/**
 * Gestionnaire du jeton CSRF pour une application monopage (documentation de Spring Security 6.5,
 * « Single-Page Applications ») : le jeton est chargé à chaque requête pour que le cookie
 * {@code XSRF-TOKEN} soit écrit, et Angular le renvoie tel quel dans l'en-tête {@code X-XSRF-TOKEN}.
 */
final class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

    private final CsrfTokenRequestHandler brut = new CsrfTokenRequestAttributeHandler();
    private final CsrfTokenRequestHandler masque = new XorCsrfTokenRequestAttributeHandler();

    @Override
    public void handle(HttpServletRequest requete, HttpServletResponse reponse, Supplier<CsrfToken> jeton) {
        // Le jeton protégé contre BREACH sert pour les formulaires rendus côté serveur (il n'y en a pas ici).
        this.masque.handle(requete, reponse, jeton);
        // Force le chargement du jeton différé, donc l'écriture du cookie dans la réponse.
        jeton.get();
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest requete, CsrfToken jeton) {
        String valeurEnTete = requete.getHeader(jeton.getHeaderName());
        return (StringUtils.hasText(valeurEnTete) ? this.brut : this.masque).resolveCsrfTokenValue(requete, jeton);
    }
}
