package fr.skillpath.commun.erreurs;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * Écrit une erreur au format RFC 9457 hors de Spring MVC, là où {@link GestionnaireErreurs} ne passe pas :
 * la chaîne de filtres de sécurité répond avant que la requête atteigne un contrôleur (401, 403).
 */
public final class EcritureProbleme {

    private static final ObjectMapper JSON = new ObjectMapper();

    private EcritureProbleme() {
    }

    public static void ecrire(HttpServletRequest requete, HttpServletResponse reponse, HttpStatus statut,
                              String titre, String detail) throws IOException {
        Map<String, Object> probleme = new LinkedHashMap<>();
        probleme.put("type", "about:blank");
        probleme.put("title", titre);
        probleme.put("status", statut.value());
        probleme.put("detail", detail);
        probleme.put("instance", requete.getRequestURI());
        reponse.setStatus(statut.value());
        reponse.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        reponse.setCharacterEncoding("UTF-8");
        JSON.writeValue(reponse.getWriter(), probleme);
    }
}
