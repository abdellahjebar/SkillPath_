package fr.skillpath.commun.journalisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.skillpath.commun.erreurs.GestionnaireErreurs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Vérifie les journaux JSON, l'identifiant de requête et l'absence de données interdites (ADR-015).
 */
@ExtendWith(OutputCaptureExtension.class)
@WebMvcTest(value = JournalisationTest.ControleurDeTest.class, properties = "logging.structured.format.console=logstash")
@Import({IdentifiantRequeteFiltre.class, GestionnaireErreurs.class, JournalisationTest.ControleurDeTest.class})
class JournalisationTest {

    private static final String FAUX_SECRET = "cle-api-secrete-0123456789";
    private static final String FAUX_TEXTE_OFFRE = "Nous recherchons un alternant pour notre equipe de Valbonne";

    @Autowired
    private MockMvc mvc;

    @Test
    void identifiantRecuValideEstRepris() throws Exception {
        mvc.perform(get("/test/ok").header("X-Request-Id", "abc-123.DEF_4"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", "abc-123.DEF_4"));
    }

    @Test
    void identifiantAbsentEstCree() throws Exception {
        mvc.perform(get("/test/ok"))
                .andExpect(header().exists("X-Request-Id"));
    }

    @Test
    void identifiantInvalideNEstPasRepris() throws Exception {
        String valeurHostile = "x\" , \"injecte\":\"oui";
        String renvoye = mvc.perform(get("/test/ok").header("X-Request-Id", valeurHostile))
                .andReturn().getResponse().getHeader("X-Request-Id");

        assertThat(renvoye).isNotEqualTo(valeurHostile).matches("[0-9a-f-]{36}");
    }

    @Test
    void identifiantTropLongNEstPasRepris() throws Exception {
        String trop = "a".repeat(65);
        String renvoye = mvc.perform(get("/test/ok").header("X-Request-Id", trop))
                .andReturn().getResponse().getHeader("X-Request-Id");

        assertThat(renvoye).isNotEqualTo(trop);
    }

    @Test
    void leContexteDeJournalisationEstNettoyeApresLaRequete() throws Exception {
        mvc.perform(get("/test/ok").header("X-Request-Id", "propre-1"));

        assertThat(MDC.get("requestId")).isNull();
    }

    @Test
    void lesJournauxSontEnJsonAvecLIdentifiantDeRequete(CapturedOutput sortie) throws Exception {
        mvc.perform(get("/test/panne").header("X-Request-Id", "trace-42"));

        String ligne = sortie.getAll().lines()
                .filter(l -> l.contains("Erreur inattendue"))
                .findFirst().orElseThrow();
        assertThat(ligne).startsWith("{").endsWith("}").contains("\"requestId\":\"trace-42\"");
    }

    @Test
    void aucunSecretNiTexteDOffreDansLesJournaux(CapturedOutput sortie) throws Exception {
        mvc.perform(get("/test/panne"));

        assertThat(sortie.getAll())
                .contains("Erreur inattendue")
                .contains("java.lang.IllegalStateException")
                .contains("JournalisationTest")
                .doesNotContain(FAUX_SECRET)
                .doesNotContain(FAUX_TEXTE_OFFRE);
    }

    @RestController
    static class ControleurDeTest {

        @GetMapping("/test/ok")
        String ok() {
            return "ok";
        }

        @GetMapping("/test/panne")
        String panne() {
            throw new IllegalStateException("echec avec " + FAUX_SECRET + " et " + FAUX_TEXTE_OFFRE);
        }
    }
}
