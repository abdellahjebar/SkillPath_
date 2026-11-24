package fr.skillpath.acces.internal;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * Limite les tentatives de connexion par compte (OWASP, Authentication Cheat Sheet : le compteur est lié
 * au compte, pas à l'adresse). Spring Security n'en fournit pas ; il publie des événements, qu'on écoute.
 * Un compte verrouillé répond comme un mauvais mot de passe : rien ne révèle le verrouillage.
 */
@Component
class LimitationDesTentatives {

    private final ComptesJdbc comptes;

    LimitationDesTentatives(ComptesJdbc comptes) {
        this.comptes = comptes;
    }

    @EventListener
    void surEchec(AuthenticationFailureBadCredentialsEvent evenement) {
        comptes.enregistrerUnEchec(evenement.getAuthentication().getName());
    }

    @EventListener
    void surReussite(AuthenticationSuccessEvent evenement) {
        comptes.remettreLesCompteursAZero(evenement.getAuthentication().getName());
    }
}
