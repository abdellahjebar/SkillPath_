package fr.skillpath.commun;

/**
 * Noms des rôles de l'application.
 *
 * <p>Ils sont dans le noyau commun pour que les autres modules n'aient pas à dépendre du
 * module {@code acces} lorsqu'ils protègent leurs contrôleurs.
 */
public final class Roles {

    public static final String RESPONSABLE = "RESPONSABLE";
    public static final String MEMBRE_CONSEIL = "MEMBRE_CONSEIL";
    public static final String ENSEIGNANT = "ENSEIGNANT";
    public static final String ADMINISTRATEUR = "ADMINISTRATEUR";

    private Roles() {
    }
}
