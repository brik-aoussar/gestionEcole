package dao;

import config.DatabaseConnection;
import exception.DAOException;
import model.Etudiant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EtudiantDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(EtudiantDAO.class);

    public Etudiant insert(Etudiant e) {
        String sql = "INSERT INTO etudiant (cne, nom, prenom, date_naissance, email, telephone, statut) VALUES (?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getCne());
            ps.setString(2, e.getNom());
            ps.setString(3, e.getPrenom());
            ps.setDate(4, e.getDateNaissance() != null ? Date.valueOf(e.getDateNaissance()) : null);
            ps.setString(5, e.getEmail());
            ps.setString(6, e.getTelephone());
            ps.setString(7, e.getStatut().name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) e.setId(rs.getLong(1));
            }
            return e;
        } catch (SQLException ex) {
            LOGGER.error("Erreur insertion etudiant", ex);
            throw new DAOException("Erreur insertion etudiant: " + ex.getMessage());
        }
    }

    public Etudiant update(Etudiant e) {
        String sql = "UPDATE etudiant SET cne=?, nom=?, prenom=?, date_naissance=?, email=?, telephone=?, statut=? WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, e.getCne());
            ps.setString(2, e.getNom());
            ps.setString(3, e.getPrenom());
            ps.setDate(4, e.getDateNaissance() != null ? Date.valueOf(e.getDateNaissance()) : null);
            ps.setString(5, e.getEmail());
            ps.setString(6, e.getTelephone());
            ps.setString(7, e.getStatut().name());
            ps.setLong(8, e.getId());
            ps.executeUpdate();
            return e;
        } catch (SQLException ex) {
            LOGGER.error("Erreur mise a jour etudiant", ex);
            throw new DAOException("Erreur mise a jour etudiant: " + ex.getMessage());
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM etudiant WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.error("Erreur suppression etudiant", ex);
            throw new DAOException("Erreur suppression etudiant: " + ex.getMessage());
        }
    }

    public Etudiant findById(Long id) {
        String sql = "SELECT * FROM etudiant WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapper(rs) : null;
            }
        } catch (SQLException ex) {
            LOGGER.error("Erreur recherche etudiant par ID", ex);
            throw new DAOException("Erreur recherche etudiant: " + ex.getMessage());
        }
    }

    public Etudiant findByCne(String cne) {
        String sql = "SELECT * FROM etudiant WHERE cne=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, cne);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapper(rs) : null;
            }
        } catch (SQLException ex) {
            LOGGER.error("Erreur recherche etudiant par CNE", ex);
            throw new DAOException("Erreur recherche etudiant: " + ex.getMessage());
        }
    }

    public List<Etudiant> findAll() {
        return findByQuery("SELECT * FROM etudiant ORDER BY nom, prenom");
    }

    public List<Etudiant> findByPromotion(Long promotionId) {
        String sql = "SELECT e.* FROM etudiant e JOIN inscription i ON e.id = i.etudiant_id WHERE i.promotion_id=? AND e.statut='ACTIF' ORDER BY e.nom, e.prenom";
        return findByQuery(sql, promotionId);
    }

    public List<Etudiant> findByFiliere(Long filiereId) {
        String sql = "SELECT e.* FROM etudiant e JOIN inscription i ON e.id = i.etudiant_id JOIN promotion p ON i.promotion_id = p.id WHERE p.filiere_id=? AND e.statut='ACTIF' ORDER BY e.nom, e.prenom";
        return findByQuery(sql, filiereId);
    }

    public List<Etudiant> search(String terme) {
        String sql = "SELECT * FROM etudiant WHERE cne LIKE ? OR nom LIKE ? OR prenom LIKE ? ORDER BY nom, prenom";
        String pattern = "%" + terme + "%";
        return findByQuery(sql, pattern, pattern, pattern);
    }

    public int insertBatch(List<Etudiant> etudiants) {
        String sql = "INSERT INTO etudiant (cne, nom, prenom, date_naissance, email, telephone, statut) VALUES (?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE nom=VALUES(nom), prenom=VALUES(prenom), email=VALUES(email), telephone=VALUES(telephone)";
        int count = 0;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (Etudiant e : etudiants) {
                ps.setString(1, e.getCne());
                ps.setString(2, e.getNom());
                ps.setString(3, e.getPrenom());
                ps.setDate(4, e.getDateNaissance() != null ? Date.valueOf(e.getDateNaissance()) : null);
                ps.setString(5, e.getEmail());
                ps.setString(6, e.getTelephone());
                ps.setString(7, e.getStatut().name());
                ps.addBatch();
                count++;
            }
            ps.executeBatch();
            return count;
        } catch (SQLException ex) {
            LOGGER.error("Erreur batch insertion etudiants", ex);
            throw new DAOException("Erreur batch insertion: " + ex.getMessage());
        }
    }

    public void inscrire(Long etudiantId, Long promotionId) {
        String sql = "INSERT INTO inscription (etudiant_id, promotion_id) VALUES (?,?) ON DUPLICATE KEY UPDATE promotion_id=VALUES(promotion_id)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, etudiantId);
            ps.setLong(2, promotionId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.error("Erreur inscription etudiant", ex);
            throw new DAOException("Erreur inscription: " + ex.getMessage());
        }
    }

    public long countAll() {
        return count("SELECT COUNT(*) FROM etudiant");
    }

    public long countByStatut(Etudiant.Statut statut) {
        return count("SELECT COUNT(*) FROM etudiant WHERE statut=?", statut.name());
    }

    private long count(String sql, Object... params) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        } catch (SQLException ex) {
            LOGGER.error("Erreur comptage etudiants", ex);
            return 0;
        }
    }

    private List<Etudiant> findByQuery(String sql, Object... params) {
        List<Etudiant> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapper(rs));
            }
        } catch (SQLException ex) {
            LOGGER.error("Erreur recherche etudiants", ex);
            throw new DAOException("Erreur recherche: " + ex.getMessage());
        }
        return list;
    }

    private Etudiant mapper(ResultSet rs) throws SQLException {
        Etudiant e = new Etudiant();
        e.setId(rs.getLong("id"));
        e.setCne(rs.getString("cne"));
        e.setNom(rs.getString("nom"));
        e.setPrenom(rs.getString("prenom"));
        Date dn = rs.getDate("date_naissance");
        if (dn != null) e.setDateNaissance(dn.toLocalDate());
        e.setEmail(rs.getString("email"));
        e.setTelephone(rs.getString("telephone"));
        e.setStatut(Etudiant.Statut.valueOf(rs.getString("statut")));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) e.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) e.setUpdatedAt(ua.toLocalDateTime());
        return e;
    }
}
