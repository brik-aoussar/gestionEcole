package service.impl;

import config.Constantes;
import dao.UtilisateurDAO;
import exception.AuthException;
import model.Utilisateur;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AuthService;

public class AuthServiceImpl implements AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UtilisateurDAO utilisateurDAO;

    public AuthServiceImpl(UtilisateurDAO utilisateurDAO) {
        this.utilisateurDAO = utilisateurDAO;
    }

    @Override
    public Utilisateur login(String login, String motDePasse) {
        if (login == null || login.isBlank() || motDePasse == null || motDePasse.isBlank()) {
            throw new AuthException(Constantes.MSG_ERREUR_CONNEXION);
        }
        Utilisateur u = utilisateurDAO.findByLogin(login);
        if (u == null) {
            LOGGER.warn("Tentative de connexion avec login inconnu: {}", login);
            throw new AuthException(Constantes.MSG_ERREUR_CONNEXION);
        }
        if (!u.isActif()) {
            LOGGER.warn("Tentative de connexion sur compte desactive: {}", login);
            throw new AuthException(Constantes.MSG_COMPTE_DESACTIVE);
        }
        if (!BCrypt.checkpw(motDePasse, u.getMotDePasse())) {
            LOGGER.warn("Mot de passe incorrect pour: {}", login);
            throw new AuthException(Constantes.MSG_ERREUR_CONNEXION);
        }
        LOGGER.info("Connexion reussie: {} ({})", login, u.getRole());
        return u;
    }

    @Override
    public void logout(Long utilisateurId) {
        LOGGER.info("Deconnexion utilisateur: {}", utilisateurId);
    }

    @Override
    public Utilisateur.Role getRole(String login) {
        Utilisateur u = utilisateurDAO.findByLogin(login);
        return u != null ? u.getRole() : null;
    }

    @Override
    public boolean verifierMotDePasse(String motDePasse, String hash) {
        return BCrypt.checkpw(motDePasse, hash);
    }

    @Override
    public String hasherMotDePasse(String motDePasse) {
        return BCrypt.hashpw(motDePasse, BCrypt.gensalt(12));
    }
}
