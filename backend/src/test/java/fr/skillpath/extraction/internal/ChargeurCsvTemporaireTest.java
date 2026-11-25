package fr.skillpath.extraction.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/** Chargement explicite des CSV, sans serveur, base de données ni contexte Spring. */
class ChargeurCsvTemporaireTest {

    private static final String ENTETE_COMPETENCES =
            "competence_id;source;uri_esco;libelle;type;dans_ensemble_c;transversale;parent_esco;statut_liste_locale\n";
    private static final String ENTETE_ALIAS = "competence_id;alias;langue;statut;provenance\n";
    private static final String COMPETENCE_SIMPLE = "C1;ESCO;;SQL;knowledge;non;non;;\n";

    @Test
    void chargeLesRessourcesDuProjetAvecLeursIdentifiantsEtLeursAlias() {
        List<CompetenceChargee> competences = new ChargeurCsvTemporaire().charger();

        assertThat(competences).isNotEmpty();
        assertThat(competences).extracting(CompetenceChargee::competenceId).doesNotHaveDuplicates();
        assertThat(competences).anySatisfy(competence -> {
            assertThat(competence.competenceId()).isEqualTo("esco:598de5b0-5b58-4ea7-8058-a4bc4d18c742");
            assertThat(competence.libelle()).isEqualTo("SQL");
            assertThat(competence.source()).isEqualTo("ESCO");
        });
        assertThat(competences).anySatisfy(competence -> {
            assertThat(competence.competenceId()).isEqualTo("LOC-003");
            assertThat(competence.libelle()).isEqualTo("Docker");
            assertThat(competence.source()).isEqualTo("liste_locale");
            assertThat(competence.type()).isEqualTo("outil");
        });
        assertThat(competences).anySatisfy(competence -> {
            assertThat(competence.competenceId()).isEqualTo("LOC-001");
            assertThat(competence.alias()).contains(
                    new AliasCharge("conteneurisation", "fr", "retenu", "liste_locale"),
                    new AliasCharge("containerization", "en", "retenu", "liste_locale"));
        });
    }

    @Test
    void conserveLesAccentsLesSeparateursGuillemetesEtLesChampsMultilignes() {
        String competences = ENTETE_COMPETENCES
                + "C1;ESCO;;\"développement;\napplications \"\"web\"\"\";skill/competence;non;non;;\n";
        String alias = ENTETE_ALIAS + "C1;\"mise en œuvre; \"\"web\"\"\";fr;retenu;ESCO\n";

        CompetenceChargee competence = chargeur(competences, alias).charger().getFirst();

        assertThat(competence.libelle()).isEqualTo("développement;\napplications \"web\"");
        assertThat(competence.type()).isEqualTo("skill/competence");
        assertThat(competence.alias()).containsExactly(
                new AliasCharge("mise en œuvre; \"web\"", "fr", "retenu", "ESCO"));
    }

    @Test
    void litLesColonnesParNomEtConserveLesLanguesEtLesStatutsSansFiltrage() {
        String competences = "\uFEFFlibelle;type;competence_id;source;dans_ensemble_c;inutile\n"
                + "développement;concept;C1;liste_locale;non;\n";
        String alias = "\uFEFFprovenance;statut;langue;alias;competence_id;inutile\n"
                + "liste_locale;retenu;fr;développer;C1;\n"
                + "liste_locale;ecarte;en;development;C1;\n";

        CompetenceChargee competence = chargeur(competences, alias).charger().getFirst();

        assertThat(competence.competenceId()).isEqualTo("C1");
        assertThat(competence.libelle()).isEqualTo("développement");
        assertThat(competence.alias()).containsExactly(
                new AliasCharge("développer", "fr", "retenu", "liste_locale"),
                new AliasCharge("development", "en", "ecarte", "liste_locale"));
    }

    @Test
    void conserveUnAntislashFinalSansLePrendrePourUneContinuation() {
        String provenance = "source\\";
        var chargeur = chargeur(ENTETE_COMPETENCES + COMPETENCE_SIMPLE,
                ENTETE_ALIAS + "C1;requête SQL;fr;retenu;" + provenance + "\n");

        assertThat(chargeur.charger().getFirst().alias()).containsExactly(
                new AliasCharge("requête SQL", "fr", "retenu", provenance));
    }

    @Test
    void refuseUnIdentifiantDeCompetenceDuplique() {
        var chargeur = chargeur(ENTETE_COMPETENCES + COMPETENCE_SIMPLE + COMPETENCE_SIMPLE, ENTETE_ALIAS);

        assertThatThrownBy(chargeur::charger)
                .isInstanceOf(IllegalStateException.class)
                .hasStackTraceContaining("competences-test.csv")
                .hasStackTraceContaining("C1")
                .hasStackTraceContaining("ligne 3");
    }

    @Test
    void refuseUnAliasDontLaCompetenceEstInconnue() {
        var chargeur = chargeur(ENTETE_COMPETENCES + COMPETENCE_SIMPLE,
                ENTETE_ALIAS + "INCONNUE;SQL;fr;retenu;ESCO\n");

        assertThatThrownBy(chargeur::charger)
                .isInstanceOf(IllegalStateException.class)
                .hasStackTraceContaining("alias-test.csv")
                .hasStackTraceContaining("INCONNUE")
                .hasStackTraceContaining("ligne 2");
    }

    @Test
    void refuseUnIdentifiantOuUnLibelleVide() {
        var identifiantVide = chargeur(ENTETE_COMPETENCES + ";ESCO;;SQL;knowledge;non;non;;\n", ENTETE_ALIAS);
        var libelleVide = chargeur(ENTETE_COMPETENCES + "C1;ESCO;;;knowledge;non;non;;\n", ENTETE_ALIAS);

        assertThatThrownBy(identifiantVide::charger)
                .isInstanceOf(IllegalStateException.class)
                .hasStackTraceContaining("competence_id")
                .hasStackTraceContaining("ligne 2");
        assertThatThrownBy(libelleVide::charger)
                .isInstanceOf(IllegalStateException.class)
                .hasStackTraceContaining("libelle")
                .hasStackTraceContaining("ligne 2");
    }

    @Test
    void refuseUnFichierSansEnteteOuSansColonneRequise() {
        var fichierVide = chargeur("", ENTETE_ALIAS);
        var colonneAbsente = chargeur("identifiant;source;libelle;type\nC1;ESCO;SQL;knowledge\n", ENTETE_ALIAS);

        assertThatThrownBy(fichierVide::charger)
                .isInstanceOf(IllegalStateException.class)
                .hasStackTraceContaining("competences-test.csv");
        assertThatThrownBy(colonneAbsente::charger)
                .isInstanceOf(IllegalStateException.class)
                .hasStackTraceContaining("competences-test.csv")
                .hasStackTraceContaining("competence_id");
    }

    @Test
    void refuseUneRessourceAbsenteAvecSonNom() {
        var chargeur = new ChargeurCsvTemporaire(
                new ClassPathResource("referentiel/competences-absentes-test.csv"),
                ressource(ENTETE_ALIAS, "alias-test.csv"));

        assertThatThrownBy(chargeur::charger)
                .isInstanceOf(IllegalStateException.class)
                .hasStackTraceContaining("competences-absentes-test.csv");
    }

    @Test
    void refuseUnChampGuillemeteNonTermine() {
        var chargeur = chargeur(ENTETE_COMPETENCES
                + "C1;ESCO;;\"développement non terminé;concept;non;non;;\n", ENTETE_ALIAS);

        assertThatThrownBy(chargeur::charger)
                .isInstanceOf(IllegalStateException.class)
                .hasStackTraceContaining("competences-test.csv");
    }

    @Test
    void permetUnNouveauChargementSansAccumulerLesAliasEtProtegeLeResultat() {
        var chargeur = chargeur(ENTETE_COMPETENCES + COMPETENCE_SIMPLE,
                ENTETE_ALIAS + "C1;requête SQL;fr;retenu;ESCO\n");

        List<CompetenceChargee> premier = chargeur.charger();
        List<CompetenceChargee> second = chargeur.charger();

        assertThat(second).isEqualTo(premier);
        assertThat(second).hasSize(1);
        assertThat(second.getFirst().alias()).hasSize(1);
        assertThatThrownBy(premier::clear).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(premier.getFirst().alias()::clear).isInstanceOf(UnsupportedOperationException.class);
    }

    private static ChargeurCsvTemporaire chargeur(String competences, String alias) {
        return new ChargeurCsvTemporaire(
                ressource(competences, "competences-test.csv"), ressource(alias, "alias-test.csv"));
    }

    private static Resource ressource(String contenu, String nom) {
        return new ByteArrayResource(contenu.getBytes(StandardCharsets.UTF_8), nom);
    }
}
