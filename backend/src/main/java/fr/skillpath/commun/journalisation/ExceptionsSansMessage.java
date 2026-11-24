package fr.skillpath.commun.journalisation;

/**
 * Prépare une exception pour le journal sans ses messages.
 *
 * <p>Le message d'une exception peut contenir une donnée interdite dans les journaux (texte d'offre,
 * secret, donnée personnelle : ADR-015). La copie garde, pour chaque exception de la chaîne, le nom
 * de la classe et la trace des appels, ce qui suffit pour retrouver l'origine d'une panne.
 */
public final class ExceptionsSansMessage {

    private ExceptionsSansMessage() {
    }

    public static Throwable depuis(Throwable origine) {
        Throwable cause = origine.getCause() == null || origine.getCause() == origine
                ? null
                : depuis(origine.getCause());
        Throwable copie = new Throwable(origine.getClass().getName(), cause);
        copie.setStackTrace(origine.getStackTrace());
        return copie;
    }
}
