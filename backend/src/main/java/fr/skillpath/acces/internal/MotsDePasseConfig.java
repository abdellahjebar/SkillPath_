package fr.skillpath.acces.internal;

import java.util.Map;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Hachage des mots de passe : Argon2id avec les paramètres minimaux de l'OWASP (19 Mio de mémoire,
 * 2 itérations, parallélisme 1). Derrière {@link DelegatingPasswordEncoder}, le hachage porte son
 * identifiant (« {argon2}… ») : on pourra changer de paramètres ou d'algorithme sans casser les comptes.
 */
@Configuration
@EnableConfigurationProperties(ParametresAcces.class)
class MotsDePasseConfig {

    private static final int OCTETS_DU_SEL = 16;
    private static final int OCTETS_DU_HACHAGE = 32;
    private static final int PARALLELISME = 1;
    private static final int MEMOIRE_EN_KIO = 19 * 1024;
    private static final int ITERATIONS = 2;

    @Bean
    PasswordEncoder encodeurDeMotsDePasse() {
        PasswordEncoder argon2 = new Argon2PasswordEncoder(
                OCTETS_DU_SEL, OCTETS_DU_HACHAGE, PARALLELISME, MEMOIRE_EN_KIO, ITERATIONS);
        return new DelegatingPasswordEncoder("argon2", Map.of("argon2", argon2));
    }
}
