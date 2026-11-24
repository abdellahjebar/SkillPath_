package fr.skillpath.acces;

import java.util.List;

/** L'utilisateur connecté, tel que l'interface le voit : identifiant et rôles (sans le préfixe « ROLE_ »). */
public record Utilisateur(String identifiant, List<String> roles) {
}
