package dao;

import config.DatabaseConnection;
import exception.DAOException;
import model.Filiere;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FiliereDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(FiliereDAO.class);

    public Filiere insert(Filiere f) {
        String sql = "INSERT INTO filiere (code, intitule, domaine) VALUES (?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, f.getCode());
            ps.setString(2, f.getIntitule());
            ps.setString(3, f.getDomaine());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) f.setId(rs.getLong(1));
            }
            return f;
        } catch (SQLException ex) {
            LOGGER.error("Erreur insertion filiere", ex);
            throw new DAOException("Erreur insertion filiere: " + ex.getMessage());
        }
    }

    public Filiere update(Filiere f) {
        String sql = "UPDATE filiere SET code=?, intitule=?, domaine=? WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, f.getCode());
            ps.setString(2, f.getIntitule());
            ps.setString(3, f.getDomaine());
            ps.setLong(4, f.getId());
            ps.executeUpdate();
            return f;
        } catch (SQLException ex) {
            LOGGER.error("Erreur mise a jour filiere", ex);
            throw new DAOException("Erreur mise a jour filiere: " + ex.getMessage());
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM filiere WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.error("Erreur suppression filiere", ex);
            throw new DAOException("Erreur suppression filiere: " + ex.getMessage());
        }
    }

    public Filiere findById(Long id) {
        String sql = "SELECT f.*, (SELECT COUNT(*) FROM promotion p WHERE p.filiere_id=f.id) as nb_promotions FROM filiere f WHERE f.id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapper(rs) : null;
            }
        } catch (SQLException ex) {
            LOGGER.error("Erreur recherche filiere", ex);
            throw new DAOException("Erreur recherche filiere: " + ex.getMessage());
        }
    }

    public List<Filiere> findAll() {
        return findByQuery("SELECT f.*, (SELECT COUNT(*) FROM promotion p WHERE p.filiere_id=f.id) as nb_promotions FROM filiere f ORDER BY f.code");
    }

    public List<Filiere> findByResponsable(Long responsableId) {
        return findByQuery("SELECT f.* FROM filiere f JOIN responsable_filiere rf ON f.id=rf.filiere_id WHERE rf.utilisateur_id=?", responsableId);
    }

    private List<Filiere> findByQuery(String sql, Object... params) {
        List<Filiere> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapper(rs));
            }
        } catch (SQLException ex) {
            LOGGER.error("Erreur recherche filieres", ex);
            throw new DAOException("Erreur recherche filieres: " + ex.getMessage());
        }
        return list;
    }

    private Filiere mapper(ResultSet rs) throws SQLException {
        Filiere f = new Filiere();
        f.setId(rs.getLong("id"));
        f.setCode(rs.getString("code"));
        f.setIntitule(rs.getString("intitule"));
        f.setDomaine(rs.getString("domaine"));
        try { f.setNombrePromotions(rs.getInt("nb_promotions")); } catch (SQLException ignored) {}
        return f;
    }
}
