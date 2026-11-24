package fr.skillpath.acces.internal;

import java.util.List;
import java.util.Set;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

/**
 * Comptes lus et écrits dans le schéma {@code acces}. Le SQL est explicite (JdbcClient, ADR-028) : les
 * compteurs de tentatives sont mis à jour par des requêtes atomiques, sans relire puis réécrire un objet.
 * Le temps vient de la base ({@code now()}), pas du serveur.
 */
@Component
class ComptesJdbc implements UserDetailsService {

    private final JdbcClient jdbc;
    private final ParametresAcces parametres;

    ComptesJdbc(JdbcClient jdbc, ParametresAcces parametres) {
        this.jdbc = jdbc;
        this.parametres = parametres;
    }

    @Override
    public UserDetails loadUserByUsername(String identifiant) throws UsernameNotFoundException {
        var compte = jdbc.sql("""
                        SELECT id, identifiant, mot_de_passe_hache, actif,
                               (verrouille_jusqua IS NOT NULL AND verrouille_jusqua > now()) AS verrouille
                        FROM acces.utilisateur WHERE lower(identifiant) = lower(?)
                        """)
                .param(identifiant)
                .query((rs, n) -> new Object[] {rs.getLong("id"), rs.getString("identifiant"),
                        rs.getString("mot_de_passe_hache"), rs.getBoolean("actif"), rs.getBoolean("verrouille")})
                .optional()
                .orElseThrow(() -> new UsernameNotFoundException("Compte inconnu"));
        List<String> roles = jdbc.sql("SELECT role FROM acces.utilisateur_role WHERE utilisateur_id = ? ORDER BY role")
                .param(compte[0]).query(String.class).list();
        return User.withUsername((String) compte[1])
                .password((String) compte[2])
                .roles(roles.toArray(String[]::new))
                .disabled(!(Boolean) compte[3])
                .accountLocked((Boolean) compte[4])
                .build();
    }

    @Transactional
    void creer(String identifiant, String motDePasseHache, Set<String> roles) {
        Long id = jdbc.sql("INSERT INTO acces.utilisateur (identifiant, mot_de_passe_hache) VALUES (?, ?) RETURNING id")
                .param(identifiant).param(motDePasseHache).query(Long.class).single();
        for (String role : roles) {
            jdbc.sql("INSERT INTO acces.utilisateur_role (utilisateur_id, role) VALUES (?, ?)").param(id).param(role).update();
        }
    }

    boolean existeUnAdministrateur() {
        return jdbc.sql("SELECT EXISTS (SELECT 1 FROM acces.utilisateur_role WHERE role = 'ADMINISTRATEUR')")
                .query(Boolean.class).single();
    }

    /**
     * Compte un échec de mot de passe : les échecs sont comptés dans une fenêtre qui s'ouvre au premier
     * échec ; au {@code tentativesMax}-ième, le compte est verrouillé. Un compte inconnu ne change rien.
     */
    void enregistrerUnEchec(String identifiant) {
        long fenetre = parametres.fenetre().toSeconds();
        jdbc.sql("""
                        UPDATE acces.utilisateur SET
                            verrouille_jusqua = CASE
                                WHEN (CASE WHEN premier_echec_le IS NULL OR premier_echec_le < now() - make_interval(secs => :fenetre)
                                           THEN 1 ELSE echecs_consecutifs + 1 END) >= :maximum
                                THEN now() + make_interval(secs => :verrouillage)
                                ELSE verrouille_jusqua END,
                            echecs_consecutifs = CASE WHEN premier_echec_le IS NULL OR premier_echec_le < now() - make_interval(secs => :fenetre)
                                                      THEN 1 ELSE echecs_consecutifs + 1 END,
                            premier_echec_le = CASE WHEN premier_echec_le IS NULL OR premier_echec_le < now() - make_interval(secs => :fenetre)
                                                    THEN now() ELSE premier_echec_le END
                        WHERE lower(identifiant) = lower(:identifiant)
                        """)
                .param("fenetre", fenetre)
                .param("maximum", parametres.tentativesMax())
                .param("verrouillage", parametres.verrouillage().toSeconds())
                .param("identifiant", identifiant)
                .update();
    }

    void remettreLesCompteursAZero(String identifiant) {
        jdbc.sql("""
                        UPDATE acces.utilisateur
                        SET echecs_consecutifs = 0, premier_echec_le = NULL, verrouille_jusqua = NULL
                        WHERE lower(identifiant) = lower(?)
                          AND (echecs_consecutifs <> 0 OR premier_echec_le IS NOT NULL OR verrouille_jusqua IS NOT NULL)
                        """)
                .param(identifiant)
                .update();
    }
}
