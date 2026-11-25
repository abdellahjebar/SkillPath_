package fr.skillpath.devdata;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fr.skillpath.extraction.TypeCorrespondance;

/**
 * Generates a small, reproducible synthetic dataset for local development.
 *
 * <p>This is test-support tooling, not part of the production application. It contains no real
 * offer text or confidential curriculum data.
 */
public final class SyntheticDatasetGenerator {

    static final long DEFAULT_SEED = 20261124L;
    static final int OFFER_COUNT = 100;
    static final int MINIMUM_ELIGIBLE_TEXT_LENGTH = 200;
    private static final Path DEFAULT_OUTPUT = Path.of("dev-data", "examples", "jeu-synthetique.json");
    private static final ObjectMapper JSON = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static final String SQL_ID = "esco:598de5b0-5b58-4ea7-8058-a4bc4d18c742";
    private static final List<SkillDefinition> SKILL_DEFINITIONS = List.of(
            new SkillDefinition(
                    "esco:19a8293b-8e95-4de3-983f-77484079c389",
                    "Java (programmation informatique)", 8, null),
            new SkillDefinition(SQL_ID, "SQL", 20, "SEQUEL"),
            new SkillDefinition("esco:a8d07b5a-c1a1-42c6-9d53-db9c7a2ca996", "PostgreSQL", 12, null));

    private SyntheticDatasetGenerator() {
    }

    public static void main(String[] args) throws IOException {
        Options options = parseOptions(args);
        Dataset dataset = generate(options.seed());
        Path output = options.output();
        Path parent = output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(output, toJson(dataset) + "\n", StandardCharsets.UTF_8);
        System.out.printf("Jeu synthetique genere : %s (graine %d, %d enregistrements, %d offres retenues)%n",
                output.toAbsolutePath().normalize(), dataset.seed(), dataset.offers().size(),
                dataset.segment().offerCount());
    }

    static Dataset generate(long seed) {
        List<LinkedHashSet<String>> skillsByOffer = new ArrayList<>(OFFER_COUNT);
        for (int i = 0; i < OFFER_COUNT; i++) {
            skillsByOffer.add(new LinkedHashSet<>());
        }

        Random random = new Random(seed);
        for (SkillDefinition skill : SKILL_DEFINITIONS) {
            List<Integer> offerIndexes = new ArrayList<>(OFFER_COUNT);
            for (int i = 0; i < OFFER_COUNT; i++) {
                offerIndexes.add(i);
            }
            Collections.shuffle(offerIndexes, random);
            for (int i = 0; i < skill.mentionCount(); i++) {
                skillsByOffer.get(offerIndexes.get(i)).add(skill.id());
            }
        }

        List<Skill> skills = SKILL_DEFINITIONS.stream()
                .map(skill -> new Skill(skill.id(), skill.label()))
                .toList();
        List<Offer> offers = new ArrayList<>(OFFER_COUNT);
        for (int i = 0; i < OFFER_COUNT; i++) {
            String id = "SYN-OFFRE-%04d".formatted(i + 1);
            Set<String> assignedSkillIds = skillsByOffer.get(i);
            List<SkillDefinition> assignedSkills = SKILL_DEFINITIONS.stream()
                    .filter(skill -> assignedSkillIds.contains(skill.id()))
                    .toList();
            int syntheticTextLength = MINIMUM_ELIGIBLE_TEXT_LENGTH + 40 + (i * 37 % 121);
            List<ExpectedMention> expectedMentions = expectedMentions(i, assignedSkills);
            offers.add(new Offer(id, syntheticTextLength,
                    syntheticTextLength >= MINIMUM_ELIGIBLE_TEXT_LENGTH, true, null, null, expectedMentions));
        }
        offers.add(new Offer("SYN-OFFRE-COURTE", MINIMUM_ELIGIBLE_TEXT_LENGTH - 1,
                false, true, null, "TEXTE_SOUS_SEUIL", List.of()));
        Offer duplicate = offers.stream()
                .filter(offer -> offer.eligible() && offer.deduplicated() && !offer.expectedMentions().isEmpty())
                .findFirst()
                .orElseThrow();
        offers.add(new Offer("SYN-OFFRE-DOUBLON", duplicate.syntheticTextLength(),
                true, false, duplicate.id(), "DOUBLON", duplicate.expectedMentions()));

        List<ProgramVersion> versions = List.of(
                new ProgramVersion("DEMO-2026-2027", "PUBLIEE", "2026-09-01", "2027-08-31",
                        unitsWithLevels(2, 1)),
                new ProgramVersion("DEMO-2027-2028", "BROUILLON", "2027-09-01", "2028-08-31",
                        unitsWithLevels(3, 2)));
        Segment segment = new Segment(
                "M18", "FRANCE", "2026-10", "2026-11-05",
                (int) offers.stream().filter(Offer::eligible).filter(Offer::deduplicated).count(),
                MINIMUM_ELIGIBLE_TEXT_LENGTH,
                "Population analytique synthetique : textes admissibles d'au moins 200 caracteres et offres dedoublonnees; exemples exclus inclus.");
        Formation formation = new Formation(
                "DEMO-FORMATION", "Formation de demonstration (fictive)", versions);

        return new Dataset(2, true, seed, segment, skills, offers, formation);
    }

    private static List<ExpectedMention> expectedMentions(int offerIndex, List<SkillDefinition> assignedSkills) {
        List<ExpectedMention> mentions = new ArrayList<>();
        for (SkillDefinition skill : assignedSkills) {
            if (skill.retainedAlias() != null && offerIndex % 4 == 0) {
                mentions.add(new ExpectedMention(skill.id(), TypeCorrespondance.ALIAS));
            } else {
                mentions.add(new ExpectedMention(skill.id(), TypeCorrespondance.LIBELLE_EXACT));
            }
            if (skill.id().equals(SQL_ID) && offerIndex % 5 == 0) {
                mentions.add(new ExpectedMention(skill.id(), TypeCorrespondance.LIBELLE_EXACT));
            }
        }
        return List.copyOf(mentions);
    }

    private static List<Unit> unitsWithLevels(int javaLevel, int postgresqlLevel) {
        return List.of(
                new Unit("DEMO-UE-CONCEPTION", "Conception et programmation", 6, List.of(
                        new Module("ECUE-JAVA-01", "Programmation Java", 18, 24, 2,
                                List.of(new Coverage(SKILL_DEFINITIONS.get(0).id(), javaLevel))),
                        new Module("ECUE-SQL-01", "Bases de donnees relationnelles", 12, 18, 1,
                                List.of(new Coverage(SKILL_DEFINITIONS.get(1).id(), 3))))),
                new Unit("DEMO-UE-DONNEES", "Systemes de donnees", 6, List.of(
                        new Module("ECUE-POSTGRESQL-01", "Administration PostgreSQL", 12, 18, 1,
                                List.of(new Coverage(SKILL_DEFINITIONS.get(2).id(), postgresqlLevel))))));
    }

    static String toJson(Dataset dataset) throws JsonProcessingException {
        return JSON.writeValueAsString(dataset);
    }

    private static Options parseOptions(String[] args) {
        long seed = DEFAULT_SEED;
        Path output = DEFAULT_OUTPUT;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--seed" -> {
                    if (++i == args.length) {
                        throw new IllegalArgumentException("Valeur manquante pour --seed");
                    }
                    seed = Long.parseLong(args[i]);
                }
                case "--output" -> {
                    if (++i == args.length) {
                        throw new IllegalArgumentException("Chemin manquant pour --output");
                    }
                    output = Path.of(args[i]);
                }
                default -> throw new IllegalArgumentException("Option inconnue : " + args[i]);
            }
        }
        return new Options(seed, output);
    }

    record Skill(String id, String label) {
    }

    record Offer(String id, int syntheticTextLength, boolean eligible, boolean deduplicated,
            String duplicateOf, String exclusionReason, List<ExpectedMention> expectedMentions) {
        Offer {
            expectedMentions = List.copyOf(expectedMentions);
        }
    }

    record ExpectedMention(String competencyId, TypeCorrespondance typeCorrespondance) {
    }

    record Coverage(String competencyId, int level) {
    }

    record Module(String stableId, String label, int hoursCm, int hoursTd, int coefficient,
            List<Coverage> competencies) {
    }

    record Unit(String id, String label, int ects, List<Module> modules) {
    }

    record ProgramVersion(String id, String status, String validFrom, String validTo, List<Unit> units) {
    }

    record Formation(String id, String label, List<ProgramVersion> versions) {
    }

    record Segment(String domainCode, String geography, String period, String collectionDate, int offerCount,
            int minimumTextLength, String populationDefinition) {
    }

    record Dataset(int schemaVersion, boolean syntheticOnly, long seed, Segment segment,
            List<Skill> competencies, List<Offer> offers, Formation formation) {
        Dataset {
            competencies = List.copyOf(competencies);
            offers = List.copyOf(offers);
        }
    }

    private record SkillDefinition(String id, String label, int mentionCount, String retainedAlias) {
    }

    private record Options(long seed, Path output) {
    }
}
