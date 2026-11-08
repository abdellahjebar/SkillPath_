package fr.skillpath;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée du serveur SkillPath Audit.
 *
 * <p>Chaque sous-paquet direct de {@code fr.skillpath} est un module (Spring Modulith).
 * Le paquet racine d'un module est son interface publique ; ses sous-paquets sont internes.
 */
@SpringBootApplication
public class SkillPathApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkillPathApplication.class, args);
    }
}
