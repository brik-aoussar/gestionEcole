package dao;

import config.DatabaseConnection;
import exception.DAOException;
import model.Module;
import model.SousModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleDAO.class);

    public Module insertModule(Module m) {
        String sql = "INSERT INTO module (code, intitule, coefficient, promotion_id) VALUES (?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getCode());
            ps.setString(2, m.getIntitule());
            ps.setDouble(3, m.getCoefficient());
            ps.setLong(4, m.getPromotionId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) m.setId(rs.getLong(1)); }
            return m;
        } catch (SQLException ex) {
            LOGGER.error("Erreur insertion module", ex);
            throw new DAOException("Erreur insertion module: " + ex.getMessage());
        }
    }

    public Module updateModule(Module m) {
        String sql = "UPDATE module SET code=?, intitule=?, coefficient=?, promotion_id=? WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, m.getCode());
            ps.setString(2, m.getIntitule());
            ps.setDouble(3, m.getCoefficient());
            ps.setLong(4, m.getPromotionId());
            ps.setLong(5, m.getId());
            ps.executeUpdate();
            return m;
        } catch (SQLException ex) {
            LOGGER.error("Erreur mise a jour module", ex);
            throw new DAOException("Erreur mise a jour module: " + ex.getMessage());
        }
    }

    public void deleteModule(Long id) {
        String sql = "DELETE FROM module WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.error("Erreur suppression module", ex);
            throw new DAOException("Erreur suppression module: " + ex.getMessage());
        }
    }

    public Module findModuleById(Long id) {
        String sql = "SELECT m.*, p.intitule as promotion_intitule FROM module m JOIN promotion p ON m.promotion_id=p.id WHERE m.id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? mapperModule(rs) : null; }
        } catch (SQLException ex) {
            LOGGER.error("Erreur recherche module", ex);
            throw new DAOException("Erreur recherche module: " + ex.getMessage());
        }
    }

    public List<Module> findByPromotion(Long promotionId) {
        return findModules("SELECT m.*, p.intitule as promotion_intitule FROM module m JOIN promotion p ON m.promotion_id=p.id WHERE m.promotion_id=? ORDER BY m.code", promotionId);
    }

    public long countModules() { return count("SELECT COUNT(*) FROM module"); }

    public SousModule insertSousModule(SousModule sm) {
        String sql = "INSERT INTO sous_module (code, intitule, coefficient, module_id, enseignant_id) VALUES (?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, sm.getCode());
            ps.setString(2, sm.getIntitule());
            ps.setDouble(3, sm.getCoefficient());
            ps.setLong(4, sm.getModuleId());
            if (sm.getEnseignantId() != null) ps.setLong(5, sm.getEnseignantId()); else ps.setNull(5, Types.BIGINT);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) sm.setId(rs.getLong(1)); }
            return sm;
        } catch (SQLException ex) {
            LOGGER.error("Erreur insertion sous-module", ex);
            throw new DAOException("Erreur insertion sous-module: " + ex.getMessage());
        }
    }

    public SousModule updateSousModule(SousModule sm) {
        String sql = "UPDATE sous_module SET code=?, intitule=?, coefficient=?, module_id=?, enseignant_id=? WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sm.getCode());
            ps.setString(2, sm.getIntitule());
            ps.setDouble(3, sm.getCoefficient());
            ps.setLong(4, sm.getModuleId());
            if (sm.getEnseignantId() != null) ps.setLong(5, sm.getEnseignantId()); else ps.setNull(5, Types.BIGINT);
            ps.setLong(6, sm.getId());
            ps.executeUpdate();
            return sm;
        } catch (SQLException ex) {
            LOGGER.error("Erreur mise a jour sous-module", ex);
            throw new DAOException("Erreur mise a jour sous-module: " + ex.getMessage());
        }
    }

    public void deleteSousModule(Long id) {
        String sql = "DELETE FROM sous_module WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.error("Erreur suppression sous-module", ex);
            throw new DAOException("Erreur suppression sous-module: " + ex.getMessage());
        }
    }

    public SousModule findSousModuleById(Long id) {
        String sql = "SELECT sm.*, m.intitule as module_intitule, CONCAT(u.nom, ' ', u.prenom) as enseignant_nom FROM sous_module sm JOIN module m ON sm.module_id=m.id LEFT JOIN enseignant e ON sm.enseignant_id=e.id LEFT JOIN utilisateur u ON e.utilisateur_id=u.id WHERE sm.id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? mapperSousModule(rs) : null; }
        } catch (SQLException ex) {
            LOGGER.error("Erreur recherche sous-module", ex);
            throw new DAOException("Erreur recherche sous-module: " + ex.getMessage());
        }
    }

    public List<SousModule> findSousModulesByModule(Long moduleId) {
        return findSousModules("SELECT sm.*, m.intitule as module_intitule, CONCAT(u.nom, ' ', u.prenom) as enseignant_nom FROM sous_module sm JOIN module m ON sm.module_id=m.id LEFT JOIN enseignant e ON sm.enseignant_id=e.id LEFT JOIN utilisateur u ON e.utilisateur_id=u.id WHERE sm.module_id=? ORDER BY sm.code", moduleId);
    }

    public List<SousModule> findByEnseignant(Long enseignantId) {
        return findSousModules("SELECT sm.*, m.intitule as module_intitule, CONCAT(u.nom, ' ', u.prenom) as enseignant_nom FROM sous_module sm JOIN module m ON sm.module_id=m.id LEFT JOIN enseignant e ON sm.enseignant_id=e.id LEFT JOIN utilisateur u ON e.utilisateur_id=u.id WHERE sm.enseignant_id=? ORDER BY sm.code", enseignantId);
    }

    public void assignerEnseignant(Long sousModuleId, Long enseignantId) {
        String sql = "UPDATE sous_module SET enseignant_id=? WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (enseignantId != null) ps.setLong(1, enseignantId); else ps.setNull(1, Types.BIGINT);
            ps.setLong(2, sousModuleId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.error("Erreur assignation enseignant", ex);
            throw new DAOException("Erreur assignation enseignant: " + ex.getMessage());
        }
    }

    public long countSousModules() { return count("SELECT COUNT(*) FROM sous_module"); }

    private List<Module> findModules(String sql, Object... params) {
        List<Module> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapperModule(rs)); }
        } catch (SQLException ex) {
            LOGGER.error("Erreur recherche modules", ex);
            throw new DAOException("Erreur recherche modules: " + ex.getMessage());
        }
        return list;
    }

    private List<SousModule> findSousModules(String sql, Object... params) {
        List<SousModule> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapperSousModule(rs)); }
        } catch (SQLException ex) {
            LOGGER.error("Erreur recherche sous-modules", ex);
            throw new DAOException("Erreur recherche sous-modules: " + ex.getMessage());
        }
        return list;
    }

    private long count(String sql, Object... params) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getLong(1) : 0; }
        } catch (SQLException ex) { return 0; }
    }

    private Module mapperModule(ResultSet rs) throws SQLException {
        Module m = new Module();
        m.setId(rs.getLong("id"));
        m.setCode(rs.getString("code"));
        m.setIntitule(rs.getString("intitule"));
        m.setCoefficient(rs.getDouble("coefficient"));
        m.setPromotionId(rs.getLong("promotion_id"));
        try { m.setPromotionIntitule(rs.getString("promotion_intitule")); } catch (SQLException ignored) {}
        return m;
    }

    private SousModule mapperSousModule(ResultSet rs) throws SQLException {
        SousModule sm = new SousModule();
        sm.setId(rs.getLong("id"));
        sm.setCode(rs.getString("code"));
        sm.setIntitule(rs.getString("intitule"));
        sm.setCoefficient(rs.getDouble("coefficient"));
        sm.setModuleId(rs.getLong("module_id"));
        long eid = rs.getLong("enseignant_id");
        if (!rs.wasNull()) sm.setEnseignantId(eid);
        try { sm.setModuleIntitule(rs.getString("module_intitule")); } catch (SQLException ignored) {}
        try { sm.setEnseignantNom(rs.getString("enseignant_nom")); } catch (SQLException ignored) {}
        return sm;
    }
}
