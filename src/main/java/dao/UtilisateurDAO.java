package dao;

import config.DatabaseConnection;
import exception.DAOException;
import model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class UtilisateurDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(UtilisateurDAO.class);

    public Utilisateur findByLogin(String login) {
        String sql = "SELECT u.*, " +
                "e.id as ens_pk, e.matricule as ens_matricule, e.specialite, e.grade, " +
                "rp.matricule as rp_matricule, rp.departement, " +
                "rf.matricule as rf_matricule, rf.filiere_id " +
                "FROM utilisateur u " +
                "LEFT JOIN enseignant e ON u.id=e.utilisateur_id " +
                "LEFT JOIN responsable_planning rp ON u.id=rp.utilisateur_id " +
                "LEFT JOIN responsable_filiere rf ON u.id=rf.utilisateur_id " +
                "WHERE u.login=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, login);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapper(rs) : null;
            }
        } catch (SQLException ex) {
            LOGGER.error("Erreur recherche utilisateur", ex);
            throw new DAOException("Erreur recherche utilisateur: " + ex.getMessage());
        }
    }

    private Utilisateur mapper(ResultSet rs) throws SQLException {
        Utilisateur.Role role = Utilisateur.Role.valueOf(rs.getString("role"));
        Utilisateur u;
        switch (role) {
            case ENSEIGNANT -> {
                Enseignant e = new Enseignant();
                // FIXED: charger le PK propre de la table enseignant pour saisi_par dans note
                long ensPk = rs.getLong("ens_pk");
                if (!rs.wasNull()) e.setEnseignantPk(ensPk);
                e.setMatricule(rs.getString("ens_matricule"));
                e.setSpecialite(rs.getString("specialite"));
                String grade = rs.getString("grade");
                if (grade != null) e.setGrade(Enseignant.Grade.valueOf(grade));
                u = e;
            }
            case RESPONSABLE_PLANNING -> {
                ResponsablePlanning rp = new ResponsablePlanning();
                rp.setMatricule(rs.getString("rp_matricule"));
                rp.setDepartement(rs.getString("departement"));
                u = rp;
            }
            case RESPONSABLE_FILIERE -> {
                ResponsableFiliere rf = new ResponsableFiliere();
                rf.setMatricule(rs.getString("rf_matricule"));
                long fid = rs.getLong("filiere_id");
                if (!rs.wasNull()) rf.setFiliereId(fid);
                u = rf;
            }
            default -> throw new DAOException("Role inconnu: " + role);
        }
        u.setId(rs.getLong("id"));
        u.setLogin(rs.getString("login"));
        u.setMotDePasse(rs.getString("mot_de_passe"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setRole(role);
        u.setActif(rs.getBoolean("actif"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) u.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) u.setUpdatedAt(ua.toLocalDateTime());
        return u;
    }
}
