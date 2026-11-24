package fr.skillpath.commun.migrations;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * Applique les migrations de chaque module dans son propre schéma (ADR-029).
 *
 * <p>Le dossier {@code db/migration/<module>/} contient les migrations d'un module, nommées
 * {@code V<n>__<description>.sql}. Le nom du dossier est le nom du schéma PostgreSQL. Chaque module a
 * son Flyway et son historique ({@code <module>.flyway_schema_history}) : la numérotation d'un module est
 * indépendante de celle des autres, et l'ajout d'une migration dans un module ne gêne pas les autres.
 * Un module sans migration ne crée aucun schéma.
 *
 * <p>La configuration automatique de Flyway dans Spring Boot est désactivée ({@code spring.flyway.enabled}).
 */
@Component
public class MigrationsParModule {

    private static final Logger JOURNAL = LoggerFactory.getLogger(MigrationsParModule.class);
    private static final Pattern NOM_DE_MODULE = Pattern.compile("[a-z][a-z0-9_]*");

    private final DataSource source;
    private final String emplacement;

    public MigrationsParModule(
            DataSource source,
            @Value("${skillpath.migrations.emplacement:classpath:db/migration}") String emplacement) {
        this.source = source;
        this.emplacement = emplacement;
    }

    @PostConstruct
    public void migrerAuDemarrage() {
        migrer();
    }

    /** Applique les migrations en attente de chaque module ; renvoie les modules qui en ont. */
    public List<String> migrer() {
        List<String> modules = decouvrirLesModules();
        for (String module : modules) {
            MigrateResult resultat = Flyway.configure()
                    .dataSource(source)
                    .locations(emplacementFlyway(module))
                    .defaultSchema(module)
                    .schemas(module)
                    .createSchemas(true)
                    .load()
                    .migrate();
            JOURNAL.info("Migrations du module {} : {} appliquée(s)", module, resultat.migrationsExecuted);
        }
        return modules;
    }

    private List<String> decouvrirLesModules() {
        TreeSet<String> modules = new TreeSet<>();
        try {
            for (Resource fichier : new PathMatchingResourcePatternResolver().getResources(emplacement + "/*/V*.sql")) {
                String[] morceaux = fichier.getURI().toString().split("/");
                String module = morceaux[morceaux.length - 2];
                if (!NOM_DE_MODULE.matcher(module).matches()) {
                    throw new IllegalStateException(
                            "Nom de dossier de migrations invalide : « " + module + " » (minuscules, chiffres et _ seulement).");
                }
                modules.add(module);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Lecture des migrations impossible dans " + emplacement, e);
        }
        return new ArrayList<>(modules);
    }

    private String emplacementFlyway(String module) {
        if (emplacement.startsWith("classpath:")) {
            return emplacement + "/" + module;
        }
        if (emplacement.startsWith("file:")) {
            return "filesystem:" + emplacement.substring("file:".length()) + "/" + module;
        }
        throw new IllegalStateException("Emplacement de migrations non pris en charge : " + emplacement);
    }
}
