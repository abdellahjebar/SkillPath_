package fr.skillpath;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Valide la pile : Spring Boot 3.5.16, Spring Data JDBC, Flyway, Spring Batch et le registre
 * d'événements de Spring Modulith, sur un vrai PostgreSQL 18 démarré en conteneur (ADR-016).
 */
@SpringBootTest(properties = {
        "spring.flyway.locations=classpath:db/test-pile",
        "spring.batch.jdbc.initialize-schema=always",
        "spring.modulith.events.jdbc.schema-initialization.enabled=true"
})
@Testcontainers
// Ferme le contexte Spring avant l'arrêt du conteneur : sinon le pool de connexions échoue à l'arrêt.
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class StackSmokeTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18");

    @Autowired
    JdbcClient jdbc;

    @Autowired
    SmokeItemRepository items;

    @Autowired
    JobLauncher launcher;

    @Autowired
    Job smokeJob;

    @Test
    void lePostgresDeTestEstEnVersion18() {
        int version = jdbc.sql("SELECT current_setting('server_version_num')::int").query(Integer.class).single();

        assertThat(version).isGreaterThanOrEqualTo(180000);
    }

    @Test
    void flywayAppliqueLaMigrationDeTest() {
        Integer applied = jdbc.sql("SELECT count(*) FROM flyway_schema_history WHERE success").query(Integer.class).single();

        assertThat(applied).isEqualTo(1);
    }

    @Test
    void springDataJdbcEnregistreEtRelitUnAgregat() {
        SmokeItem saved = items.save(new SmokeItem(null, "essai"));

        assertThat(saved.id()).isNotNull();
        assertThat(items.findById(saved.id())).contains(new SmokeItem(saved.id(), "essai"));
    }

    @Test
    void uneInsertionIdempotenteNeCreeQuUneLigne() {
        String sql = """
                INSERT INTO smoke_offre (source, id_source, date_collecte, texte)
                VALUES (:source, :idSource, :date, :texte)
                ON CONFLICT DO NOTHING
                """;
        LocalDate jour = LocalDate.of(2026, 11, 23);

        int premiere = jdbc.sql(sql).param("source", "lba").param("idSource", "abc").param("date", jour).param("texte", "t").update();
        int seconde = jdbc.sql(sql).param("source", "lba").param("idSource", "abc").param("date", jour).param("texte", "t").update();

        assertThat(premiere).isEqualTo(1);
        assertThat(seconde).isZero();
        assertThat(jdbc.sql("SELECT count(*) FROM smoke_offre").query(Integer.class).single()).isEqualTo(1);
    }

    @Test
    void unTravailRelanceAvecLesMemesParametresNEstPasRejoue() throws Exception {
        JobParameters parametres = new JobParametersBuilder()
                .addString("dateCollecte", "2026-11-23")
                .addString("source", "lba")
                .toJobParameters();

        JobExecution premiere = launcher.run(smokeJob, parametres);

        assertThat(premiere.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThatThrownBy(() -> launcher.run(smokeJob, parametres))
                .isInstanceOf(JobInstanceAlreadyCompleteException.class);
    }

    @Test
    void leRegistreDEvenementsDeSpringModulithEstCree() {
        String table = jdbc.sql("SELECT to_regclass('event_publication')::text").query(String.class).single();

        assertThat(table).isEqualTo("event_publication");
    }

    @TestConfiguration
    static class JobDeTest {

        @Bean
        Job smokeJob(JobRepository repository, PlatformTransactionManager transactions) {
            return new JobBuilder("smokeJob", repository)
                    .start(new StepBuilder("smokeStep", repository)
                            .tasklet((contribution, context) -> RepeatStatus.FINISHED, transactions)
                            .build())
                    .build();
        }
    }
}
