package fr.skillpath.commun.erreurs;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Vérifie que chaque erreur sort au format RFC 9457 du contrat, sans détail interne.
 */
@WebMvcTest(GestionnaireErreursTest.ControleurDeTest.class)
@Import({GestionnaireErreurs.class, GestionnaireErreursTest.ControleurDeTest.class})
class GestionnaireErreursTest {

    private static final String MESSAGE_INTERNE = "mot-de-passe-de-la-base-secret";

    @Autowired
    private MockMvc mvc;

    @Test
    void parametreManquantDonneUneErreur400AuFormatProbleme() throws Exception {
        mvc.perform(get("/test/periode"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.detail").isNotEmpty())
                .andExpect(jsonPath("$.instance").value("/test/periode"));
    }

    @Test
    void methodeNonPermiseDonneUneErreur405AuFormatProbleme() throws Exception {
        mvc.perform(post("/test/periode"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void ressourceInconnueDonneUneErreur404AuFormatProbleme() throws Exception {
        mvc.perform(get("/test/inconnue"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void exceptionInattendueDonneUne500GeneriqueSansFuite() throws Exception {
        String corps = mvc.perform(get("/test/panne"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.title").value("Erreur interne"))
                .andExpect(jsonPath("$.instance").value("/test/panne"))
                .andReturn().getResponse().getContentAsString();

        org.assertj.core.api.Assertions.assertThat(corps)
                .doesNotContain(MESSAGE_INTERNE)
                .doesNotContain("IllegalStateException")
                .doesNotContain("at fr.skillpath");
    }

    @RestController
    static class ControleurDeTest {

        @GetMapping("/test/periode")
        String periode(@RequestParam String periode) {
            return periode;
        }

        @GetMapping("/test/panne")
        String panne() {
            throw new IllegalStateException(MESSAGE_INTERNE);
        }
    }
}
