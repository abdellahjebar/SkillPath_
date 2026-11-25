package fr.skillpath.extraction;

/**
 * Interface publique du module Extraction : la seule porte d'entrée pour les autres modules.
 *
 * <p>Le même contrat s'applique aux textes d'offres et aux descriptions de modules pédagogiques.
 * L'association à une offre et l'enregistrement des résultats sont des opérations distinctes.
 */
public interface ExtractionApi {

    /**
     * Recherche les mentions de compétences dans le texte original, sans le modifier.
     *
     * <p>Les positions retournées portent sur le texte original, avant toute normalisation. Chaque
     * occurrence est conservée, même si la même compétence apparaît plusieurs fois.
     *
     * @param texte texte original à analyser, non nul
     * @return les mentions et les versions utilisées ; la liste est vide pour un texte vide ou sans
     *         compétence détectée
     * @throws NullPointerException si le texte est nul
     */
    ResultatExtraction extraire(String texte);
}
