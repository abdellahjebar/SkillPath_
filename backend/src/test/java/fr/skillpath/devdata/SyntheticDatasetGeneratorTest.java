package fr.skillpath.devdata;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class SyntheticDatasetGeneratorTest {

    private static final long SEED = 20261124L;

    @Test
    void laMemeGraineProduitLeMemeJson() throws Exception {
        String first = SyntheticDatasetGenerator.toJson(SyntheticDatasetGenerator.generate(SEED));
        String second = SyntheticDatasetGenerator.toJson(SyntheticDatasetGenerator.generate(SEED));

        assertThat(second).isEqualTo(first);
    }

    @Test
    void leJeuContientCentOffresEtLesEffectifsDeReference() {
        SyntheticDatasetGenerator.Dataset dataset = SyntheticDatasetGenerator.generate(SEED);
        Map<String, Long> mentions = dataset.offers().stream()
                .flatMap(offer -> offer.competencyIds().stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        assertThat(dataset.offers()).hasSize(SyntheticDatasetGenerator.OFFER_COUNT);
        assertThat(dataset.offers().stream().map(SyntheticDatasetGenerator.Offer::id).distinct()).hasSize(100);
        assertThat(dataset.segment().offerCount()).isEqualTo(dataset.offers().size());
        assertThat(mentions)
                .containsEntry("esco:19a8293b-8e95-4de3-983f-77484079c389", 8L)
                .containsEntry("esco:598de5b0-5b58-4ea7-8058-a4bc4d18c742", 20L)
                .containsEntry("esco:a8d07b5a-c1a1-42c6-9d53-db9c7a2ca996", 12L);
    }

    @Test
    void lesCompetencesUtilisentLesIdentifiantsDuReferentielVersionne() throws IOException {
        SyntheticDatasetGenerator.Dataset dataset = SyntheticDatasetGenerator.generate(SEED);
        Set<String> referenceIds = readCompetenceIds();
        Set<String> datasetIds = dataset.competencies().stream()
                .map(SyntheticDatasetGenerator.Skill::id)
                .collect(Collectors.toSet());

        assertThat(referenceIds).containsAll(datasetIds);
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
    void leJsonEstValideEtNeContientAucunTexteDeCoursOuDonneePersonnelle() throws Exception {
        String json = SyntheticDatasetGenerator.toJson(SyntheticDatasetGenerator.generate(SEED));
        JsonNode root = new ObjectMapper().readTree(json);

        assertThat(root.path("syntheticOnly").asBoolean()).isTrue();
        assertThat(root.path("segment").path("offerCount").asInt()).isEqualTo(100);
        assertThat(json)
                .doesNotContain("\"texte\"", "\"text\"", "\"professeur\"", "\"professor\"",
                        "\"description\"", "\"email\"", "\"telephone\"");
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

    private static Set<String> stableModuleIds(SyntheticDatasetGenerator.ProgramVersion version) {
        return version.units().stream()
                .flatMap(unit -> unit.modules().stream())
                .map(SyntheticDatasetGenerator.Module::stableId)
                .collect(Collectors.toSet());
    }
}
