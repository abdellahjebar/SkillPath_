package fr.skillpath.commun.migrations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.sql.DataSource;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Règles des migrations (ADR-029), sur un vrai PostgreSQL 18 : un schéma et un historique par module,
 * numérotation indépendante, erreurs claires sur les mauvais usages.
 */
@Testcontainers
class MigrationsParModuleTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18");

    @TempDir
    Path dossier;

    DataSource source;
    JdbcClient jdbc;

    @BeforeEach
    void baseVide() {
        source = new DriverManagerDataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        jdbc = JdbcClient.create(source);
        for (String schema : List.of("alpha", "beta")) {
            jdbc.sql("DROP SCHEMA IF EXISTS " + schema + " CASCADE").update();
        }
    }

    private void migration(String module, String nom, String sql) throws Exception {
        Path d = dossier.resolve(module);
        Files.createDirectories(d);
        Files.writeString(d.resolve(nom), sql);
    }

    private MigrationsParModule migrations() {
        return new MigrationsParModule(source, "file:" + dossier);
    }

    private boolean existe(String objet) {
        return jdbc.sql("SELECT to_regclass(?) IS NOT NULL").param(objet).query(Boolean.class).single();
    }

    @Test
    void chaqueModuleAUnSchemaEtUnHistoriqueAvecSaPropreNumerotation() throws Exception {
        migration("alpha", "V1__item.sql", "CREATE TABLE item(id int);");
        migration("beta", "V1__item.sql", "CREATE TABLE item(id int);");

        assertThat(migrations().migrer()).containsExactly("alpha", "beta");

        assertThat(existe("alpha.item")).isTrue();
        assertThat(existe("beta.item")).isTrue();
        assertThat(existe("alpha.flyway_schema_history")).isTrue();
        assertThat(existe("beta.flyway_schema_history")).isTrue();
        assertThat(existe("public.item")).isFalse();
    }

    @Test
    void uneMigrationAjouteeDansUnModuleNeBloquePasLesAutres() throws Exception {
        migration("alpha", "V1__a.sql", "CREATE TABLE a1(id int);");
        migration("beta", "V5__b.sql", "CREATE TABLE b5(id int);");
        migrations().migrer();

        // Avec un historique commun, V2 après V5 déjà appliquée aurait été refusée.
        migration("alpha", "V2__a.sql", "CREATE TABLE a2(id int);");
        migrations().migrer();

        assertThat(existe("alpha.a2")).isTrue();
        assertThat(existe("beta.b5")).isTrue();
    }

    @Test
    void rejouerSansNouveauteNeFaitRien() throws Exception {
        migration("alpha", "V1__a.sql", "CREATE TABLE a1(id int);");
        migrations().migrer();

        migrations().migrer();

        assertThat(jdbc.sql("SELECT count(*) FROM alpha.flyway_schema_history WHERE version IS NOT NULL")
                .query(Integer.class).single()).isEqualTo(1);
    }

    @Test
    void uneMigrationModifieeApresApplicationEstRefusee() throws Exception {
        migration("alpha", "V1__a.sql", "CREATE TABLE a1(id int);");
        migrations().migrer();

        migration("alpha", "V1__a.sql", "CREATE TABLE a1(id int, ajout int);");

        assertThatThrownBy(() -> migrations().migrer())
                .isInstanceOf(FlywayException.class)
                .hasMessageContaining("checksum mismatch");
    }

    @Test
    void deuxMigrationsDeMemeVersionDansUnModuleSontRefusees() throws Exception {
        migration("alpha", "V1__a.sql", "CREATE TABLE a1(id int);");
        migration("alpha", "V1__autre.sql", "CREATE TABLE a2(id int);");

        assertThatThrownBy(() -> migrations().migrer())
                .isInstanceOf(FlywayException.class)
                .hasMessageContaining("Found more than one migration with version 1");
    }

    @Test
    void unNomDeDossierInvalideEstRefuse() throws Exception {
        migration("Alpha-1", "V1__a.sql", "CREATE TABLE a1(id int);");

        assertThatThrownBy(() -> migrations().migrer())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Nom de dossier de migrations invalide");
    }

    @Test
    void sansMigrationAucunSchemaNEstCree() {
        assertThat(migrations().migrer()).isEmpty();
        assertThat(jdbc.sql("SELECT count(*) FROM information_schema.schemata WHERE schema_name IN ('alpha','beta')")
                .query(Integer.class).single()).isZero();
    }

    @Test
    void lesMigrationsDuDepotSAppliquentSurUneBaseVide() {
        // Emplacement réel du dépôt : échoue dès qu'une migration est invalide ou en collision.
        MigrationsParModule reel = new MigrationsParModule(source, "classpath:db/migration");

        assertThat(reel.migrer()).isNotNull();
    }
}
