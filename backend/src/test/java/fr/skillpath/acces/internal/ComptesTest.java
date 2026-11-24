package fr.skillpath.acces.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import fr.skillpath.commun.Roles;
import jakarta.servlet.http.Cookie;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Comptes réels (ADR-013) sur un PostgreSQL 18 : migration du schéma, hachage Argon2id, connexion par
 * l'API, limitation des tentatives par compte.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ComptesTest {

    private static final String MOT_DE_PASSE = "une-phrase-de-passe-solide";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18");

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcClient jdbc;
    @Autowired
    ComptesJdbc comptes;
    @Autowired
    PasswordEncoder encodeur;

    @BeforeEach
    void aucunCompte() {
        jdbc.sql("DELETE FROM acces.utilisateur").update();
        comptes.creer("Marie.Durand", encodeur.encode(MOT_DE_PASSE), Set.of(Roles.ENSEIGNANT));
    }

    /** Ce que fait l'interface : un GET pour obtenir le cookie CSRF, puis le POST avec l'en-tête. */
    private MvcResult connecter(String identifiant, String motDePasse) throws Exception {
        Cookie jeton = mvc.perform(get("/api/v1/session")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        return mvc.perform(post("/api/v1/session")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("identifiant", identifiant).param("motDePasse", motDePasse)
                        .cookie(jeton).header("X-XSRF-TOKEN", jeton.getValue()))
                .andReturn();
    }

    private int statut(String identifiant, String motDePasse) throws Exception {
        return connecter(identifiant, motDePasse).getResponse().getStatus();
    }

    private void echouer(int fois) throws Exception {
        for (int i = 0; i < fois; i++) {
            assertThat(statut("marie.durand", "mauvais-mot-de-passe")).isEqualTo(401);
        }
    }

    @Test
    void laMigrationCreeLesTablesDuSchemaAcces() {
        assertThat(jdbc.sql("SELECT to_regclass('acces.utilisateur') IS NOT NULL AND to_regclass('acces.utilisateur_role') IS NOT NULL")
                .query(Boolean.class).single()).isTrue();
        assertThat(jdbc.sql("SELECT count(*) FROM acces.flyway_schema_history WHERE success").query(Integer.class).single())
                .isGreaterThanOrEqualTo(1);
    }

    @Test
    void leMotDePasseEstStockeEnArgon2id() {
        String hache = jdbc.sql("SELECT mot_de_passe_hache FROM acces.utilisateur").query(String.class).single();

        assertThat(hache).startsWith("{argon2}$argon2id$").doesNotContain(MOT_DE_PASSE);
        assertThat(encodeur.matches(MOT_DE_PASSE, hache)).isTrue();
    }

    @Test
    void uneConnexionAvecUnCompteReelRenvoieSesRoles() throws Exception {
        MvcResult connexion = connecter("MARIE.durand", MOT_DE_PASSE);

        assertThat(connexion.getResponse().getStatus()).isEqualTo(200);
        assertThat(connexion.getResponse().getContentAsString()).contains("\"roles\":[\"ENSEIGNANT\"]");
    }

    @Test
    void unCompteInconnuEtUnMauvaisMotDePasseRepondentPareil() throws Exception {
        String mauvais = connecter("marie.durand", "faux").getResponse().getContentAsString();
        String inconnu = connecter("personne", "faux").getResponse().getContentAsString();

        assertThat(mauvais).isEqualTo(inconnu.replace("/api/v1/session", "/api/v1/session"));
        assertThat(mauvais).contains("Identifiant ou mot de passe incorrect.");
    }

    @Test
    void cinqEchecsVerrouillentLeCompteMemeAvecLeBonMotDePasse() throws Exception {
        echouer(5);

        MvcResult verrouille = connecter("marie.durand", MOT_DE_PASSE);

        assertThat(verrouille.getResponse().getStatus()).isEqualTo(401);
        assertThat(verrouille.getResponse().getContentAsString()).contains("Identifiant ou mot de passe incorrect.");
    }

    @Test
    void quatreEchecsNeVerrouillentPasEtLaReussiteRemetLeCompteurAZero() throws Exception {
        echouer(4);
        assertThat(statut("marie.durand", MOT_DE_PASSE)).isEqualTo(200);

        assertThat(jdbc.sql("SELECT echecs_consecutifs FROM acces.utilisateur").query(Integer.class).single()).isZero();
        echouer(4);
        assertThat(statut("marie.durand", MOT_DE_PASSE)).isEqualTo(200);
    }

    @Test
    void leCompteSeDeverrouilleApresLaDuree() throws Exception {
        echouer(5);
        jdbc.sql("UPDATE acces.utilisateur SET verrouille_jusqua = now() - interval '1 second'").update();

        assertThat(statut("marie.durand", MOT_DE_PASSE)).isEqualTo(200);
        assertThat(jdbc.sql("SELECT verrouille_jusqua IS NULL FROM acces.utilisateur").query(Boolean.class).single()).isTrue();
    }

    @Test
    void desEchecsEspacesAuDelaDeLaFenetreNeVerrouillentPas() throws Exception {
        echouer(4);
        // La fenêtre de 15 minutes s'est refermée : le prochain échec rouvre une fenêtre et compte pour 1.
        jdbc.sql("UPDATE acces.utilisateur SET premier_echec_le = now() - interval '16 minutes'").update();
        echouer(1);

        assertThat(jdbc.sql("SELECT echecs_consecutifs FROM acces.utilisateur").query(Integer.class).single()).isEqualTo(1);
        assertThat(statut("marie.durand", MOT_DE_PASSE)).isEqualTo(200);
    }

    @Test
    void unCompteDesactiveNePeutPasSeConnecter() throws Exception {
        jdbc.sql("UPDATE acces.utilisateur SET actif = false").update();

        assertThat(statut("marie.durand", MOT_DE_PASSE)).isEqualTo(401);
    }

    @Test
    void deuxComptesDeMemeIdentifiantALaCasseProsSontRefuses() {
        assertThatThrownBy(() -> comptes.creer("MARIE.DURAND", encodeur.encode(MOT_DE_PASSE), Set.of(Roles.MEMBRE_CONSEIL)))
                .isInstanceOf(org.springframework.dao.DuplicateKeyException.class);
    }

    @Test
    void lePasDeComptePrefabriqueExiste() {
        assertThat(jdbc.sql("SELECT count(*) FROM acces.utilisateur WHERE lower(identifiant) IN ('user','admin')")
                .query(Integer.class).single()).isZero();
    }
}
