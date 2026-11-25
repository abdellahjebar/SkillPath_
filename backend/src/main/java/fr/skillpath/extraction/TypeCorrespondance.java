package fr.skillpath.extraction;

/**
 * Catégories de correspondance du dictionnaire, utilisées pour expliquer la confiance accordée à une
 * détection ; elles ne représentent pas des probabilités.
 */
public enum TypeCorrespondance {

    /** Correspondance avec le libellé préféré d'une compétence. */
    LIBELLE_EXACT,

    /** Correspondance avec un alias retenu par le Référentiel. */
    ALIAS,

    /** Correspondance avec un terme ambigu dont les conditions de détection sont satisfaites. */
    TERME_AMBIGU
}
