package fr.skillpath.acces.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.skillpath.commun.erreurs.EcritureProbleme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * Sécurité de l'API (ADR-013) : session par cookie, protection CSRF pour une application monopage,
 * refus par défaut, erreurs au format RFC 9457. Les autres modules protègent leurs contrôleurs par rôle
 * avec {@code @PreAuthorize} et les constantes de {@code Roles}.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecuriteConfig {

    private static final String SESSION = "/api/v1/session";

    @Bean
    SecurityFilterChain chaineDeSecurite(HttpSecurity http, ObjectMapper json) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))
                .authorizeHttpRequests(acces -> acces
                        .requestMatchers(HttpMethod.POST, SESSION).permitAll()
                        .anyRequest().authenticated())
                .formLogin(connexion -> connexion
                        .loginPage(SESSION)
                        .loginProcessingUrl(SESSION)
                        .usernameParameter("identifiant")
                        .passwordParameter("motDePasse")
                        .successHandler((requete, reponse, authentification) -> {
                            reponse.setStatus(HttpStatus.OK.value());
                            reponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            reponse.setCharacterEncoding("UTF-8");
                            json.writeValue(reponse.getWriter(), UtilisateurConnecte.depuis(authentification));
                        })
                        // Même message que l'identifiant soit inconnu ou le mot de passe faux.
                        .failureHandler((requete, reponse, erreur) -> EcritureProbleme.ecrire(
                                requete, reponse, HttpStatus.UNAUTHORIZED, "Non connecté",
                                "Identifiant ou mot de passe incorrect.")))
                .logout(deconnexion -> deconnexion
                        .logoutRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.DELETE, SESSION))
                        .logoutSuccessHandler((requete, reponse, authentification) ->
                                reponse.setStatus(HttpStatus.NO_CONTENT.value())))
                .exceptionHandling(erreurs -> erreurs
                        .authenticationEntryPoint((requete, reponse, erreur) -> EcritureProbleme.ecrire(
                                requete, reponse, HttpStatus.UNAUTHORIZED, "Non connecté",
                                "Une session est nécessaire."))
                        .accessDeniedHandler((requete, reponse, erreur) -> EcritureProbleme.ecrire(
                                requete, reponse, HttpStatus.FORBIDDEN, "Accès interdit",
                                "Votre rôle ne permet pas cette action.")));
        return http.build();
    }
}
