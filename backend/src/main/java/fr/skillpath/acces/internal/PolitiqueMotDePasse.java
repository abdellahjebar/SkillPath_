package fr.skillpath.acces.internal;

/**
 * Règles de longueur des mots de passe (OWASP, Authentication Cheat Sheet) : au moins 15 caractères
 * quand il n'y a pas de double authentification, ce qui est le cas de la première version (ADR-013),
 * et un maximum de 128 pour permettre les phrases de passe sans exposer le serveur à des entrées énormes.
 */
final class PolitiqueMotDePasse {

    static final int LONGUEUR_MINIMALE = 15;
    static final int LONGUEUR_MAXIMALE = 128;

    private PolitiqueMotDePasse() {
    }

    static void verifier(String motDePasse) {
        int longueur = motDePasse == null ? 0 : motDePasse.codePointCount(0, motDePasse.length());
        if (longueur < LONGUEUR_MINIMALE) {
            throw new IllegalArgumentException(
                    "Le mot de passe doit avoir au moins " + LONGUEUR_MINIMALE + " caractères.");
        }
        if (longueur > LONGUEUR_MAXIMALE) {
            throw new IllegalArgumentException(
                    "Le mot de passe doit avoir au plus " + LONGUEUR_MAXIMALE + " caractères.");
        }
    }
}
