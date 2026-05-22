package service;

import exception.AuthException;
import model.Utilisateur;

public interface AuthService {
    Utilisateur login(String login, String password) throws AuthException;
    String hasherMotDePasse(String motDePasse);
    boolean verifierMotDePasse(String motDePasse, String hash);
}
