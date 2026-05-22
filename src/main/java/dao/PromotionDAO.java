package dao;

import config.DatabaseConnection;
import exception.DAOException;
import model.Promotion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PromotionDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(PromotionDAO.class);

    public Promotion insert(Promotion p) {
        String sql = "INSERT INTO promotion (intitule, annee, filiere_id, actif) VALUES (?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getIntitule());
            ps.setInt(2, p.getAnnee());
            ps.setLong(3, p.getFiliereId());
            ps.setBoolean(4, p.isActif());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) p.setId(rs.getLong(1));
            }
            return p;
        } catch (SQLException ex) {
            LOGGER.error("Erreur insertion promotion", ex);
            throw new DAOException("Erreur insertion promotion: " + ex.getMessage());
        }
    }

    public Promotion update(Promotion p) {
        String sql = "UPDATE promotion SET intitule=?, annee=?, filiere_id=?, actif=? WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getIntitule());
            ps.setInt(2, p.getAnnee());
            ps.setLong(3, p.getFiliereId());
            ps.setBoolean(4, p.isActif());
            ps.setLong(5, p.getId());
            ps.executeUpdate();
            return p;
        } catch (SQLException ex) {
            LOGGER.error("Erreur mise a jour promotion", ex);
            throw new DAOException("Erreur mise a jour promotion: " + ex.getMessage());
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM promotion WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.error("Erreur suppression promotion", ex);
            throw new DAOException("Erreur suppression promotion: " + ex.getMessage());
        }
    }

    public Promotion findById(Long id) {
        String sql = "SELECT p.*, f.intitule as filiere_intitule, (SELECT COUNT(*) FROM inscription i WHERE i.promotion_id=p.id) as nb_etudiants, (SELECT COUNT(*) FROM module m WHERE m.promotion_id=p.id) as nb_modules FROM promotion p JOIN filiere f ON p.filiere_id=f.id WHERE p.id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapper(rs) : null;
            }
        } catch (SQLException ex) {
            LOGGER.error("Erreur recherche promotion", ex);
            throw new DAOException("Erreur recherche promotion: " + ex.getMessage());
        }
    }

    public List<Promotion> findAll() {
        return findByQuery("SELECT p.*, f.intitule as filiere_intitule FROM promotion p JOIN filiere f ON p.filiere_id=f.id ORDER BY p.annee DESC, p.intitule");
    }

    public List<Promotion> findByFiliere(Long filiereId) {
        return findByQuery("SELECT p.*, f.intitule as filiere_intitule FROM promotion p JOIN filiere f ON p.filiere_id=f.id WHERE p.filiere_id=? ORDER BY p.annee DESC", filiereId);
    }

    public List<Promotion> findActives() {
        return findByQuery("SELECT p.*, f.intitule as filiere_intitule FROM promotion p JOIN filiere f ON p.filiere_id=f.id WHERE p.actif=true ORDER BY p.annee DESC");
    }

    public long countAll() {
        return count("SELECT COUNT(*) FROM promotion");
    }

    private long count(String sql, Object... params) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        } catch (SQLException ex) {
            LOGGER.error("Erreur comptage promotions", ex);
            return 0;
        }
    }

    private List<Promotion> findByQuery(String sql, Object... params) {
        List<Promotion> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapper(rs));
            }
        } catch (SQLException ex) {
            LOGGER.error("Erreur recherche promotions", ex);
            throw new DAOException("Erreur recherche promotions: " + ex.getMessage());
        }
        return list;
    }

    private Promotion mapper(ResultSet rs) throws SQLException {
        Promotion p = new Promotion();
        p.setId(rs.getLong("id"));
        p.setIntitule(rs.getString("intitule"));
        p.setAnnee(rs.getInt("annee"));
        p.setFiliereId(rs.getLong("filiere_id"));
        p.setActif(rs.getBoolean("actif"));
        try { p.setFiliereIntitule(rs.getString("filiere_intitule")); } catch (SQLException ignored) {}
        try { p.setNombreEtudiants(rs.getInt("nb_etudiants")); } catch (SQLException ignored) {}
        try { p.setNombreModules(rs.getInt("nb_modules")); } catch (SQLException ignored) {}
        return p;
    }
}
