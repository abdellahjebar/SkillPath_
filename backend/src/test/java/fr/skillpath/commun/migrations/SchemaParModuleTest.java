package fr.skillpath.commun.migrations;

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

/**
 * Le serveur applique les migrations de chaque module au démarrage, et Spring Data JDBC lit et écrit
 * dans le schéma du module ({@code @Table(schema = ...)}).
 */
@SpringBootTest(properties = "skillpath.migrations.emplacement=classpath:db/migration-essai")
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SchemaParModuleTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18");

    @Autowired
    SchemaItemRepository items;

    @Autowired
    JdbcClient jdbc;

    @Test
    void springDataJdbcEcritEtLitDansLeSchemaDuModule() {
        SchemaItem enregistre = items.save(new SchemaItem(null, "essai"));

        assertThat(items.findById(enregistre.id())).contains(new SchemaItem(enregistre.id(), "essai"));
        assertThat(jdbc.sql("SELECT label FROM alpha.item WHERE id = ?").param(enregistre.id())
                .query(String.class).single()).isEqualTo("essai");
    }

    @Test
    void lHistoriqueDuModuleEstDansSonSchema() {
        assertThat(jdbc.sql("SELECT count(*) FROM alpha.flyway_schema_history WHERE success AND version = '1'")
                .query(Integer.class).single()).isEqualTo(1);
    }
}
