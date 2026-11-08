package fr.skillpath;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Gardien des frontières entre modules.
 *
 * <p>Échoue si un module en appelle un autre sans passer par son interface publique, s'il y a un
 * cycle entre modules, ou si une dépendance n'est pas déclarée dans {@code @ApplicationModule}.
 */
class ModularityTests {

    private static final ApplicationModules MODULES = ApplicationModules.of(SkillPathApplication.class);

    @Test
    void verifieLesFrontieresEntreModules() {
        MODULES.verify();
    }

    @Test
    void detecteLesSeptModulesEtLeNoyauCommun() {
        MODULES.forEach(module -> System.out.println("MODULE DETECTE : " + module.getIdentifier()));

        assertThat(MODULES.stream().map(module -> module.getIdentifier().toString()))
                .containsExactlyInAnyOrder(
                        "commun", "referentiel", "acces", "ingestion",
                        "extraction", "programmes", "analytique", "pipeline");
    }
}
