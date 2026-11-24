package fr.skillpath.commun.journalisation;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Donne un identifiant à chaque requête : il est placé dans le contexte de journalisation (MDC),
 * donc dans chaque ligne de journal de la requête, et renvoyé dans l'en-tête de la réponse.
 *
 * <p>L'en-tête entrant est repris seulement s'il est court et ne contient que des caractères sûrs ;
 * sinon un identifiant est créé. Une valeur arbitraire ne doit pas entrer dans les journaux.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class IdentifiantRequeteFiltre extends OncePerRequestFilter {

    public static final String EN_TETE = "X-Request-Id";
    public static final String CLE_MDC = "requestId";

    private static final Pattern VALEUR_SURE = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    @Override
    protected void doFilterInternal(HttpServletRequest requete, HttpServletResponse reponse, FilterChain chaine)
            throws ServletException, IOException {
        String recu = requete.getHeader(EN_TETE);
        String identifiant = recu != null && VALEUR_SURE.matcher(recu).matches()
                ? recu
                : UUID.randomUUID().toString();
        MDC.put(CLE_MDC, identifiant);
        reponse.setHeader(EN_TETE, identifiant);
        try {
            chaine.doFilter(requete, reponse);
        } finally {
            MDC.remove(CLE_MDC);
        }
    }
}
