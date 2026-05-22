package service;

import model.Utilisateur;

public interface AuthService {
    Utilisateur login(String login, String motDePasse);
    void logout(Long utilisateurId);
    Utilisateur.Role getRole(String login);
    boolean verifierMotDePasse(String motDePasse, String hash);
    String hasherMotDePasse(String motDePasse);
}
