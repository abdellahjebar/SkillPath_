package fr.skillpath.extraction;

/**
 * Une occurrence de compétence reconnue dans le texte original.
 *
 * <p>Les positions suivent les indices UTF-16 de {@link String} : le début est inclus et la fin est
 * exclue. Le passage correspond à {@code texte.substring(debut, fin)}, avant normalisation.
 *
 * @param competenceId identifiant de la compétence fourni par le Référentiel
 * @param passage expression reconnue, telle qu'elle apparaît dans le texte original
 * @param debut indice inclus du premier caractère du passage
 * @param fin indice exclu situé immédiatement après le passage
 * @param typeCorrespondance origine de la correspondance pour le moteur à dictionnaire
 */
public record MentionDetectee(
        String competenceId,
        String passage,
        int debut,
        int fin,
        TypeCorrespondance typeCorrespondance) {
}
