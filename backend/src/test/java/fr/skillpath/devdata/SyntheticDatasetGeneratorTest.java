package fr.skillpath.devdata;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import fr.skillpath.extraction.TypeCorrespondance;

class SyntheticDatasetGeneratorTest {

    private static final long SEED = 20261124L;
    private static final String SQL_ID = "esco:598de5b0-5b58-4ea7-8058-a4bc4d18c742";

    @Test
    void laMemeGraineProduitLeMemeJson() throws Exception {
        String first = SyntheticDatasetGenerator.toJson(SyntheticDatasetGenerator.generate(SEED));
        String second = SyntheticDatasetGenerator.toJson(SyntheticDatasetGenerator.generate(SEED));

        assertThat(second).isEqualTo(first);
    }

    @Test
    void leJeuContientCentOffresEtLesEffectifsParOffreDeReference() {
        SyntheticDatasetGenerator.Dataset dataset = SyntheticDatasetGenerator.generate(SEED);
        List<SyntheticDatasetGenerator.Offer> includedOffers = includedOffers(dataset);
        Map<String, Long> mentions = includedOffers.stream()
                .flatMap(offer -> offer.expectedMentions().stream()
                        .map(SyntheticDatasetGenerator.ExpectedMention::competencyId)
                        .distinct())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        assertThat(includedOffers).hasSize(SyntheticDatasetGenerator.OFFER_COUNT);
        assertThat(dataset.offers()).hasSize(SyntheticDatasetGenerator.OFFER_COUNT + 2);
        assertThat(dataset.offers().stream().map(SyntheticDatasetGenerator.Offer::id).distinct())
                .hasSize(SyntheticDatasetGenerator.OFFER_COUNT + 2);
        assertThat(dataset.segment().offerCount()).isEqualTo(includedOffers.size());
        assertThat(dataset.segment().minimumTextLength())
                .isEqualTo(SyntheticDatasetGenerator.MINIMUM_ELIGIBLE_TEXT_LENGTH);
        assertThat(mentions)
                .containsEntry("esco:19a8293b-8e95-4de3-983f-77484079c389", 8L)
                .containsEntry(SQL_ID, 20L)
                .containsEntry("esco:a8d07b5a-c1a1-42c6-9d53-db9c7a2ca996", 12L);
    }

    @Test
    void lesOffresSynthetiquesSontEligiblesEtLesOccurrencesSontDocumentees() {
        SyntheticDatasetGenerator.Dataset dataset = SyntheticDatasetGenerator.generate(SEED);
        List<SyntheticDatasetGenerator.Offer> includedOffers = includedOffers(dataset);
        Set<String> includedIds = includedOffers.stream()
                .map(SyntheticDatasetGenerator.Offer::id)
                .collect(Collectors.toSet());

        assertThat(includedOffers).allSatisfy(offer -> {
            assertThat(offer.eligible()).isTrue();
            assertThat(offer.deduplicated()).isTrue();
            assertThat(offer.syntheticTextLength())
                    .isGreaterThanOrEqualTo(SyntheticDatasetGenerator.MINIMUM_ELIGIBLE_TEXT_LENGTH);
        });
        assertThat(dataset.offers()).anySatisfy(offer -> {
            assertThat(offer.eligible()).isFalse();
            assertThat(offer.syntheticTextLength())
                    .isLessThan(SyntheticDatasetGenerator.MINIMUM_ELIGIBLE_TEXT_LENGTH);
            assertThat(offer.exclusionReason()).isEqualTo("TEXTE_SOUS_SEUIL");
        });
        assertThat(dataset.offers()).anySatisfy(offer -> {
            assertThat(offer.deduplicated()).isFalse();
            assertThat(offer.duplicateOf()).isNotBlank();
            assertThat(includedIds).contains(offer.duplicateOf());
            assertThat(offer.exclusionReason()).isEqualTo("DOUBLON");
        });
        SyntheticDatasetGenerator.Offer duplicate = dataset.offers().stream()
                .filter(offer -> "DOUBLON".equals(offer.exclusionReason()))
                .findFirst()
                .orElseThrow();
        SyntheticDatasetGenerator.Offer original = dataset.offers().stream()
                .filter(offer -> offer.id().equals(duplicate.duplicateOf()))
                .findFirst()
                .orElseThrow();
        assertThat(duplicate.expectedMentions()).isEqualTo(original.expectedMentions());
    }

    @Test
    void lesMentionsAttenduesCouvrentLesAliasEtLesOccurrencesRepetees() throws IOException {
        SyntheticDatasetGenerator.Dataset dataset = SyntheticDatasetGenerator.generate(SEED);
        List<SyntheticDatasetGenerator.Offer> includedOffers = includedOffers(dataset);
        var mentions = includedOffers.stream()
                .flatMap(offer -> offer.expectedMentions().stream())
                .toList();

        assertThat(mentions).anySatisfy(mention -> {
            assertThat(mention.competencyId()).isEqualTo(SQL_ID);
            assertThat(mention.typeCorrespondance()).isEqualTo(TypeCorrespondance.ALIAS);
        });
        assertThat(hasRetainedAlias(SQL_ID, "SEQUEL")).isTrue();
        assertThat(includedOffers).anySatisfy(offer -> {
            long distinctSkills = offer.expectedMentions().stream()
                    .map(SyntheticDatasetGenerator.ExpectedMention::competencyId)
                    .distinct()
                    .count();
            assertThat(offer.expectedMentions()).hasSizeGreaterThan((int) distinctSkills);
        });
        assertThat(dataset.offers()).anySatisfy(offer ->
                assertThat(offer.expectedMentions()).isEmpty());
    }

    @Test
    void lesCompetencesUtilisentLesIdentifiantsDuReferentielVersionne() throws IOException {
        SyntheticDatasetGenerator.Dataset dataset = SyntheticDatasetGenerator.generate(SEED);
        Set<String> referenceIds = readCompetenceIds();
        Set<String> datasetIds = dataset.competencies().stream()
                .map(SyntheticDatasetGenerator.Skill::id)
                .collect(Collectors.toSet());
        Set<String> annotatedIds = dataset.offers().stream()
                .flatMap(offer -> offer.expectedMentions().stream())
                .map(SyntheticDatasetGenerator.ExpectedMention::competencyId)
                .collect(Collectors.toSet());

        assertThat(referenceIds).containsAll(datasetIds);
        assertThat(referenceIds).containsAll(annotatedIds);
    }

    @Test
    void leProgrammeFictifRespecteLesVersionsEtLesLiensDeCompetences() {
        SyntheticDatasetGenerator.Dataset dataset = SyntheticDatasetGenerator.generate(SEED);
        SyntheticDatasetGenerator.ProgramVersion published = dataset.formation().versions().get(0);
        SyntheticDatasetGenerator.ProgramVersion draft = dataset.formation().versions().get(1);
        Set<String> knownSkills = dataset.competencies().stream()
                .map(SyntheticDatasetGenerator.Skill::id)
                .collect(Collectors.toSet());

        assertThat(published.status()).isEqualTo("PUBLIEE");
        assertThat(draft.status()).isEqualTo("BROUILLON");
        assertThat(LocalDate.parse(published.validTo())).isBefore(LocalDate.parse(draft.validFrom()));
        assertThat(YearMonth.from(LocalDate.parse(dataset.segment().collectionDate()).minusMonths(1)).toString())
                .isEqualTo(dataset.segment().period());

        assertThat(stableModuleIds(draft)).containsExactlyInAnyOrderElementsOf(stableModuleIds(published));
        assertThat(published.units()).allSatisfy(unit -> assertThat(unit.ects()).isPositive());
        assertThat(published.units().stream()
                .flatMap(unit -> unit.modules().stream())
                .flatMap(module -> module.competencies().stream()))
                .allSatisfy(coverage -> {
                    assertThat(knownSkills).contains(coverage.competencyId());
                    assertThat(coverage.level()).isBetween(1, 3);
                });
    }

    @Test
    void leJsonEstValideEtNeContientAucunTexteDOffreOuDonneePersonnelle() throws Exception {
        String json = SyntheticDatasetGenerator.toJson(SyntheticDatasetGenerator.generate(SEED));
        JsonNode root = new ObjectMapper().readTree(json);

        assertThat(root.path("schemaVersion").asInt()).isEqualTo(2);
        assertThat(root.path("syntheticOnly").asBoolean()).isTrue();
        assertThat(root.path("segment").path("offerCount").asInt()).isEqualTo(100);
        assertThat(root.path("offers").get(0).path("syntheticTextLength").asInt())
                .isGreaterThanOrEqualTo(SyntheticDatasetGenerator.MINIMUM_ELIGIBLE_TEXT_LENGTH);
        assertThat(json)
                .doesNotContain("\"text\"", "\"passage\"", "\"debut\"", "\"fin\"",
                        "\"professeur\"", "\"professor\"", "\"courseDescription\"",
                        "\"email\"", "\"telephone\"", "\"phone\"", "\"company\"");
    }

    private static Set<String> readCompetenceIds() throws IOException {
        InputStream resource = SyntheticDatasetGeneratorTest.class.getResourceAsStream(
                "/referentiel/competence.csv");
        assertThat(resource).isNotNull();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .skip(1)
                    .map(line -> line.substring(0, line.indexOf(';')))
                    .collect(Collectors.toSet());
        }
    }

    private static List<SyntheticDatasetGenerator.Offer> includedOffers(
            SyntheticDatasetGenerator.Dataset dataset) {
        return dataset.offers().stream()
                .filter(SyntheticDatasetGenerator.Offer::eligible)
                .filter(SyntheticDatasetGenerator.Offer::deduplicated)
                .toList();
    }

    private static boolean hasRetainedAlias(String competencyId, String alias) throws IOException {
        InputStream resource = SyntheticDatasetGeneratorTest.class.getResourceAsStream(
                "/referentiel/alias_competence.csv");
        assertThat(resource).isNotNull();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .skip(1)
                    .map(line -> line.split(";", -1))
                    .anyMatch(columns -> columns.length >= 4
                            && columns[0].equals(competencyId)
                            && columns[1].equals(alias)
                            && columns[3].equals("retenu"));
        }
    }

    private static Set<String> stableModuleIds(SyntheticDatasetGenerator.ProgramVersion version) {
        return version.units().stream()
                .flatMap(unit -> unit.modules().stream())
                .map(SyntheticDatasetGenerator.Module::stableId)
                .collect(Collectors.toSet());
    }
}
