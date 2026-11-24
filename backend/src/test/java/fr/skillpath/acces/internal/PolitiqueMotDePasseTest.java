package fr.skillpath.acces.internal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/** Longueurs de mot de passe de l'OWASP (au moins 15 sans double authentification, au plus 128). */
class PolitiqueMotDePasseTest {

    @Test
    void quatorzeCaracteresSontRefuses() {
        assertThatThrownBy(() -> PolitiqueMotDePasse.verifier("a".repeat(14)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("au moins 15");
    }

    @Test
    void quinzeEtCentVingtHuitCaracteresPassent() {
        assertThatCode(() -> PolitiqueMotDePasse.verifier("a".repeat(15))).doesNotThrowAnyException();
        assertThatCode(() -> PolitiqueMotDePasse.verifier("a".repeat(128))).doesNotThrowAnyException();
    }

    @Test
    void centVingtNeufCaracteresSontRefuses() {
        assertThatThrownBy(() -> PolitiqueMotDePasse.verifier("a".repeat(129)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("au plus 128");
    }

    @Test
    void ledecompteSeFaitEnCaracteresEtNonEnOctets() {
        // 15 caractères accentués = 30 octets : accepté.
        assertThatCode(() -> PolitiqueMotDePasse.verifier("é".repeat(15))).doesNotThrowAnyException();
    }

    @Test
    void absentOuVideEstRefuse() {
        assertThatThrownBy(() -> PolitiqueMotDePasse.verifier(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PolitiqueMotDePasse.verifier("")).isInstanceOf(IllegalArgumentException.class);
    }
}
