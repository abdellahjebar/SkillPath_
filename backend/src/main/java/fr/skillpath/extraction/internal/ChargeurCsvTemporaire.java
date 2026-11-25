package fr.skillpath.extraction.internal;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.separator.DefaultRecordSeparatorPolicy;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Accès provisoire aux deux CSV du Référentiel, en attendant son interface fonctionnelle.
 *
 * <p>Le chargement est explicite : {@code new ChargeurCsvTemporaire().charger()}. Aucune lecture
 * n'est déclenchée au démarrage et aucune donnée n'est écrite en base. Le lecteur CSV de Spring
 * Batch est déjà disponible dans le projet ; aucun job Batch n'est créé ici.
 */
public class ChargeurCsvTemporaire implements SourceCompetences {

    private final Resource ressourceCompetences;
    private final Resource ressourceAlias;

    public ChargeurCsvTemporaire() {
        this(new ClassPathResource("referentiel/competence.csv"),
                new ClassPathResource("referentiel/alias_competence.csv"));
    }

    /** Permet de charger d'autres ressources, notamment des exemples pour les tests. */
    public ChargeurCsvTemporaire(Resource competences, Resource alias) {
        this.ressourceCompetences = Objects.requireNonNull(competences);
        this.ressourceAlias = Objects.requireNonNull(alias);
    }

    @Override
    public List<CompetenceChargee> charger() {
        Map<String, CompetenceChargee> competences = new LinkedHashMap<>();
        Map<String, List<AliasCharge>> aliasParCompetence = new HashMap<>();

        lire(ressourceCompetences, Set.of("competence_id", "libelle", "source", "type"), ligne -> {
            String id = requis(ressourceCompetences, ligne, "competence_id");
            String libelle = requis(ressourceCompetences, ligne, "libelle");
            var competence = new CompetenceChargee(id, libelle,
                    ligne.champs().readRawString("source"), ligne.champs().readRawString("type"), List.of());
            if (competences.putIfAbsent(id, competence) != null) {
                throw erreur(ressourceCompetences, ligne.numero(), "identifiant de compétence dupliqué : " + id);
            }
        });

        lire(ressourceAlias, Set.of("competence_id", "alias", "langue", "statut", "provenance"), ligne -> {
            String id = requis(ressourceAlias, ligne, "competence_id");
            if (!competences.containsKey(id)) {
                throw erreur(ressourceAlias, ligne.numero(), "alias rattaché à une compétence inconnue : " + id);
            }
            var alias = new AliasCharge(requis(ressourceAlias, ligne, "alias"),
                    requis(ressourceAlias, ligne, "langue"), requis(ressourceAlias, ligne, "statut"),
                    ligne.champs().readRawString("provenance"));
            aliasParCompetence.computeIfAbsent(id, cle -> new ArrayList<>()).add(alias);
        });

        // L'ordre des compétences est celui du CSV. Tous les statuts d'alias sont conservés ; le
        // choix des termes utilisables appartient à la préparation du dictionnaire.
        return competences.values().stream()
                .map(c -> new CompetenceChargee(c.competenceId(), c.libelle(), c.source(), c.type(),
                        aliasParCompetence.getOrDefault(c.competenceId(), List.of())))
                .toList();
    }

    private static void lire(Resource ressource, Set<String> colonnesRequises, Consumer<LigneCsv> traiter) {
        var tokenizer = new DelimitedLineTokenizer(";");
        var lecteur = new FlatFileItemReader<LigneCsv>();
        lecteur.setResource(ressource);
        lecteur.setEncoding(StandardCharsets.UTF_8.name());
        lecteur.setStrict(true);
        lecteur.setSaveState(false);
        lecteur.setComments(new String[0]);
        lecteur.setLinesToSkip(1);
        lecteur.setSkippedLinesCallback(entete -> {
            if (entete == null) {
                throw erreur(ressource, 1, "en-tête CSV absent");
            }
            String sansBom = entete.startsWith("\uFEFF") ? entete.substring(1) : entete;
            String[] noms = new DelimitedLineTokenizer(";").tokenize(sansBom).getValues();
            Set<String> colonnes = new HashSet<>();
            for (String nom : noms) {
                if (nom.isBlank() || !colonnes.add(nom)) {
                    throw erreur(ressource, 1, "en-tête CSV avec une colonne vide ou dupliquée : " + nom);
                }
            }
            if (!colonnes.containsAll(colonnesRequises)) {
                Set<String> manquantes = new HashSet<>(colonnesRequises);
                manquantes.removeAll(colonnes);
                throw erreur(ressource, 1, "colonnes manquantes dans l'en-tête CSV : " + manquantes);
            }
            tokenizer.setNames(noms);
        });
        lecteur.setRecordSeparatorPolicy(new DefaultRecordSeparatorPolicy() {
            @Override
            public boolean isEndOfRecord(String record) {
                // Seuls des guillemets non fermés prolongent un champ CSV sur la ligne suivante.
                // Un antislash en fin de champ reste une donnée, pas un caractère de continuation.
                return record.chars().filter(caractere -> caractere == '"').count() % 2 == 0;
            }
        });
        lecteur.setLineMapper((ligne, numero) -> new LigneCsv(tokenizer.tokenize(ligne), numero));

        try {
            lecteur.afterPropertiesSet();
            lecteur.open(new ExecutionContext());
            LigneCsv ligne;
            while ((ligne = lecteur.read()) != null) {
                traiter.accept(ligne);
            }
        } catch (FlatFileParseException exception) {
            throw new IllegalStateException(messageErreur(ressource, exception.getLineNumber(),
                    "enregistrement CSV invalide"), exception);
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Impossible de lire le CSV " + ressource.getDescription(), exception);
        } finally {
            lecteur.close();
        }
    }

    private static String requis(Resource ressource, LigneCsv ligne, String colonne) {
        String valeur = ligne.champs().readRawString(colonne);
        if (valeur == null || valeur.isBlank()) {
            throw erreur(ressource, ligne.numero(), "valeur obligatoire absente : " + colonne);
        }
        return valeur;
    }

    private static IllegalStateException erreur(Resource ressource, int ligne, String raison) {
        return new IllegalStateException(messageErreur(ressource, ligne, raison));
    }

    private static String messageErreur(Resource ressource, int ligne, String raison) {
        return "CSV " + ressource.getDescription() + ", ligne " + ligne + " : " + raison;
    }

    private record LigneCsv(FieldSet champs, int numero) {
    }
}
