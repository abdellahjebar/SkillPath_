package fr.skillpath.acces.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.skillpath.acces.internal.web.SessionController;
import fr.skillpath.commun.Roles;
import fr.skillpath.commun.erreurs.GestionnaireErreurs;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sécurité de l'API (ADR-013) : session, protection CSRF pour une application monopage, refus par
 * défaut, erreurs RFC 9457, rôles. Les comptes sont ceux du test : le serveur n'en a pas encore.
 */
@WebMvcTest(controllers = {SecuriteTest.ControleurProtege.class, SessionController.class})
@Import({SecuriteConfig.class, GestionnaireErreurs.class, SecuriteTest.ControleurProtege.class,
        SessionController.class, SecuriteTest.Comptes.class})
class SecuriteTest {

    @Autowired
    MockMvc mvc;

    @TestConfiguration
    static class Comptes {
        @Bean
        UserDetailsService comptesDeTest() {
            var encodeur = PasswordEncoderFactories.createDelegatingPasswordEncoder();
            return new InMemoryUserDetailsManager(
                    User.withUsername("admin").password(encodeur.encode("motdepasse-admin-1234"))
                            .roles(Roles.ADMINISTRATEUR).build(),
                    User.withUsername("membre").password(encodeur.encode("motdepasse-membre-1234"))
                            .roles(Roles.MEMBRE_CONSEIL).build());
        }
    }

    @RestController
    static class ControleurProtege {
        @GetMapping("/api/v1/test/administration")
        @PreAuthorize("hasRole('" + Roles.ADMINISTRATEUR + "')")
        String administration() {
            return "ok";
        }

        @GetMapping("/api/v1/test/lecture")
        String lecture() {
            return "ok";
        }
    }

    /** Déroule ce que fait Angular : GET pour obtenir le cookie CSRF, puis POST avec l'en-tête. */
    private MvcResult connecter(String identifiant, String motDePasse) throws Exception {
        MvcResult premier = mvc.perform(get("/api/v1/session")).andReturn();
        Cookie jeton = premier.getResponse().getCookie("XSRF-TOKEN");
        assertThat(jeton).as("cookie XSRF-TOKEN écrit dès le premier échange").isNotNull();
        return mvc.perform(post("/api/v1/session")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("identifiant", identifiant)
                        .param("motDePasse", motDePasse)
                        .cookie(jeton)
                        .header("X-XSRF-TOKEN", jeton.getValue()))
                .andReturn();
    }

    @Test
    void sansSessionLApiRepond401AuFormatProbleme() throws Exception {
        mvc.perform(get("/api/v1/test/lecture"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("Non connecté"));
    }

    @Test
    void toutCheminInconnuEstRefuseParDefaut() throws Exception {
        mvc.perform(get("/nimporte/quoi")).andExpect(status().isUnauthorized());
    }

    @Test
    void leCookieCsrfEstLisibleParLeNavigateur() throws Exception {
        mvc.perform(get("/api/v1/session"))
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andExpect(cookie().httpOnly("XSRF-TOKEN", false));
    }

    @Test
    void uneConnexionSansJetonCsrfEstRefusee() throws Exception {
        mvc.perform(post("/api/v1/session")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("identifiant", "admin").param("motDePasse", "motdepasse-admin-1234"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void uneConnexionReussieRenvoieLUtilisateurEtSesRoles() throws Exception {
        MvcResult connexion = connecter("admin", "motdepasse-admin-1234");

        assertThat(connexion.getResponse().getStatus()).isEqualTo(200);
        assertThat(connexion.getResponse().getContentAsString())
                .contains("\"identifiant\":\"admin\"").contains("\"roles\":[\"ADMINISTRATEUR\"]")
                .doesNotContain("motdepasse");
    }

    @Test
    void uneConnexionRefuseeNeDitPasSiLIdentifiantExiste() throws Exception {
        String mauvaisMotDePasse = connecter("admin", "faux").getResponse().getContentAsString();
        String identifiantInconnu = connecter("inconnu", "faux").getResponse().getContentAsString();

        assertThat(mauvaisMotDePasse).contains("\"status\":401").contains("Identifiant ou mot de passe incorrect.");
        assertThat(identifiantInconnu).contains("Identifiant ou mot de passe incorrect.");
    }

    @Test
    void laSessionOuverteDonneAccesEtLIdentifiantDeSessionChangeALaConnexion() throws Exception {
        MockHttpSession avant = new MockHttpSession();
        String idAvant = avant.getId();
        MvcResult premier = mvc.perform(get("/api/v1/session").session(avant)).andReturn();
        Cookie jeton = premier.getResponse().getCookie("XSRF-TOKEN");

        MvcResult connexion = mvc.perform(post("/api/v1/session")
                        .session(avant)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("identifiant", "admin").param("motDePasse", "motdepasse-admin-1234")
                        .cookie(jeton).header("X-XSRF-TOKEN", jeton.getValue()))
                .andExpect(status().isOk()).andReturn();
        MockHttpSession apres = (MockHttpSession) connexion.getRequest().getSession(false);

        assertThat(apres).isNotNull();
        assertThat(apres.getId()).as("protection contre la fixation de session").isNotEqualTo(idAvant);
        mvc.perform(get("/api/v1/session").session(apres))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identifiant").value("admin"));
    }

    @Test
    void unRoleInsuffisantDonne403AuFormatProbleme() throws Exception {
        MockHttpSession session = (MockHttpSession) connecter("membre", "motdepasse-membre-1234")
                .getRequest().getSession(false);

        mvc.perform(get("/api/v1/test/administration").session(session))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Accès interdit"));
        mvc.perform(get("/api/v1/test/lecture").session(session)).andExpect(status().isOk());
    }

    @Test
    void leBonRolePasse() throws Exception {
        MockHttpSession session = (MockHttpSession) connecter("admin", "motdepasse-admin-1234")
                .getRequest().getSession(false);

        mvc.perform(get("/api/v1/test/administration").session(session)).andExpect(status().isOk());
    }

    @Test
    void laDeconnexionFermeLaSession() throws Exception {
        MockHttpSession session = (MockHttpSession) connecter("admin", "motdepasse-admin-1234")
                .getRequest().getSession(false);
        MvcResult etat = mvc.perform(get("/api/v1/session").session(session)).andReturn();
        Cookie jeton = etat.getResponse().getCookie("XSRF-TOKEN");

        mvc.perform(delete("/api/v1/session").session(session)
                        .header("X-XSRF-TOKEN", jeton == null ? "" : jeton.getValue())
                        .cookie(jeton == null ? new Cookie("XSRF-TOKEN", "") : jeton))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/test/lecture").session(session)).andExpect(status().isUnauthorized());
    }
}
