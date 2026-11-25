package fr.skillpath.extraction;

import java.util.List;

/**
 * Résultat d'une extraction et métadonnées permettant d'identifier le traitement effectué.
 *
 * @param mentions occurrences détectées, éventuellement vide ; une compétence peut apparaître
 *         plusieurs fois
 * @param versionExtracteur version du moteur qui a produit les mentions
 * @param versionReferentiel version du référentiel effectivement utilisée
 * @param versionListeLocale version du complément local effectivement utilisée
 * @param empreinteConfiguration empreinte des paramètres de détection utilisés
 */
public record ResultatExtraction(
        List<MentionDetectee> mentions,
        String versionExtracteur,
        String versionReferentiel,
        String versionListeLocale,
        String empreinteConfiguration) {

    /**
     * Conserve une copie non modifiable des mentions.
     *
     * @throws NullPointerException si la liste ou l'une de ses mentions est nulle
     */
    public ResultatExtraction {
        mentions = List.copyOf(mentions);
    }
}
