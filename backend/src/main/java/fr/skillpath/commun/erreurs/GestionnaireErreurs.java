package fr.skillpath.commun.erreurs;

import fr.skillpath.commun.journalisation.ExceptionsSansMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Transforme toute erreur du serveur en réponse RFC 9457 ({@code application/problem+json}), au
 * format du schéma {@code Probleme} du contrat d'API (ADR-012).
 *
 * <p>Les exceptions de Spring MVC (paramètre invalide, ressource inconnue, méthode non permise)
 * sont prises en charge par la classe de base. Toute autre exception devient une erreur 500
 * générique : ni message, ni trace, ni nom de classe ne sortent vers le client.
 *
 * <p>Les réponses 401 et 403 relèvent de Spring Security (module Accès) et ne passent pas ici.
 */
@RestControllerAdvice
public class GestionnaireErreurs extends ResponseEntityExceptionHandler {

    private static final Logger JOURNAL = LoggerFactory.getLogger(GestionnaireErreurs.class);

    @ExceptionHandler(Exception.class)
    ProblemDetail erreurInattendue(Exception exception, HttpServletRequest requete) {
        JOURNAL.error("Erreur inattendue sur {} {}", requete.getMethod(), requete.getRequestURI(),
                ExceptionsSansMessage.depuis(exception));
        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur interne est survenue.");
        probleme.setTitle("Erreur interne");
        probleme.setInstance(java.net.URI.create(requete.getRequestURI()));
        return probleme;
    }
}
