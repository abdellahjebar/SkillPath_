package fr.skillpath.acces.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Le premier administrateur est créé une seule fois depuis la configuration, avec un mot de passe haché. */
@SpringBootTest(properties = {
        "skillpath.acces.administrateur.identifiant=chef",
        "skillpath.acces.administrateur.mot-de-passe=une-phrase-de-passe-solide"
})
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PremierAdministrateurTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18");

    @Autowired
    JdbcClient jdbc;
    @Autowired
    PremierAdministrateur premierAdministrateur;

    @Test
    void lAdministrateurExisteAvecSonRoleEtUnMotDePasseHache() {
        assertThat(jdbc.sql("SELECT role FROM acces.utilisateur_role r JOIN acces.utilisateur u ON u.id = r.utilisateur_id WHERE u.identifiant = 'chef'")
                .query(String.class).single()).isEqualTo("ADMINISTRATEUR");
        assertThat(jdbc.sql("SELECT mot_de_passe_hache FROM acces.utilisateur WHERE identifiant = 'chef'")
                .query(String.class).single()).startsWith("{argon2}$argon2id$").doesNotContain("une-phrase");
    }

    @Test
    void relancerNeCreeAucunDoublon() {
        premierAdministrateur.run(null);
        premierAdministrateur.run(null);

        assertThat(jdbc.sql("SELECT count(*) FROM acces.utilisateur").query(Integer.class).single()).isEqualTo(1);
    }
}
