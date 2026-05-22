package dao;

import config.DatabaseConnection;
import exception.DAOException;
import model.Note;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoteDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoteDAO.class);

    public Note insert(Note n) {
        String sql = "INSERT INTO note (valeur, type_note, etudiant_id, sous_module_id, saisi_par, validee) VALUES (?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDouble(1, n.getValeur());
            ps.setString(2, n.getTypeNote().name());
            ps.setLong(3, n.getEtudiantId());
            ps.setLong(4, n.getSousModuleId());
            if (n.getSaisiPar() != null) ps.setLong(5, n.getSaisiPar()); else ps.setNull(5, Types.BIGINT);
            ps.setBoolean(6, n.isValidee());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) n.setId(rs.getLong(1)); }
            return n;
        } catch (SQLException ex) {
            LOGGER.error("Erreur insertion note", ex);
            throw new DAOException("Erreur insertion note: " + ex.getMessage());
        }
    }

    public Note update(Note n) {
        String sql = "UPDATE note SET valeur=?, type_note=?, etudiant_id=?, sous_module_id=?, saisi_par=?, validee=? WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, n.getValeur());
            ps.setString(2, n.getTypeNote().name());
            ps.setLong(3, n.getEtudiantId());
            ps.setLong(4, n.getSousModuleId());
            if (n.getSaisiPar() != null) ps.setLong(5, n.getSaisiPar()); else ps.setNull(5, Types.BIGINT);
            ps.setBoolean(6, n.isValidee());
            ps.setLong(7, n.getId());
            ps.executeUpdate();
            return n;
        } catch (SQLException ex) {
            LOGGER.error("Erreur mise a jour note", ex);
            throw new DAOException("Erreur mise a jour note: " + ex.getMessage());
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM note WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.error("Erreur suppression note", ex);
            throw new DAOException("Erreur suppression note: " + ex.getMessage());
        }
    }

    public Note findById(Long id) {
        String sql = "SELECT n.*, e.nom as etudiant_nom, e.cne as etudiant_cne, sm.intitule as sous_module_intitule, m.intitule as module_intitule FROM note n JOIN etudiant e ON n.etudiant_id=e.id JOIN sous_module sm ON n.sous_module_id=sm.id JOIN module m ON sm.module_id=m.id WHERE n.id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? mapper(rs) : null; }
        } catch (SQLException ex) {
            LOGGER.error("Erreur recherche note", ex);
            throw new DAOException("Erreur recherche note: " + ex.getMessage());
        }
    }

    public List<Note> findByEtudiant(Long etudiantId) {
        return findByQuery("SELECT n.*, e.nom as etudiant_nom, e.cne as etudiant_cne, sm.intitule as sous_module_intitule, m.intitule as module_intitule FROM note n JOIN etudiant e ON n.etudiant_id=e.id JOIN sous_module sm ON n.sous_module_id=sm.id JOIN module m ON sm.module_id=m.id WHERE n.etudiant_id=? ORDER BY m.intitule, sm.intitule", etudiantId);
    }

    public List<Note> findBySousModule(Long sousModuleId) {
        return findByQuery("SELECT n.*, e.nom as etudiant_nom, e.cne as etudiant_cne, sm.intitule as sous_module_intitule, m.intitule as module_intitule FROM note n JOIN etudiant e ON n.etudiant_id=e.id JOIN sous_module sm ON n.sous_module_id=sm.id JOIN module m ON sm.module_id=m.id WHERE n.sous_module_id=? ORDER BY e.nom, e.prenom", sousModuleId);
    }

    public List<Note> findByPromotion(Long promotionId) {
        return findByQuery("SELECT n.*, e.nom as etudiant_nom, e.cne as etudiant_cne, sm.intitule as sous_module_intitule, m.intitule as module_intitule FROM note n JOIN etudiant e ON n.etudiant_id=e.id JOIN sous_module sm ON n.sous_module_id=sm.id JOIN module m ON sm.module_id=m.id WHERE m.promotion_id=? ORDER BY e.nom, m.intitule", promotionId);
    }

    public void validerParSousModule(Long sousModuleId) {
        String sql = "UPDATE note SET validee=true WHERE sous_module_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, sousModuleId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.error("Erreur validation notes", ex);
            throw new DAOException("Erreur validation notes: " + ex.getMessage());
        }
    }

    public double calculerMoyenneGenerale(Long etudiantId, Long promotionId) {
        String sql = "SELECT SUM(n.valeur * sm.coefficient) / SUM(sm.coefficient) as moyenne FROM note n JOIN sous_module sm ON n.sous_module_id=sm.id JOIN module m ON sm.module_id=m.id WHERE n.etudiant_id=? AND m.promotion_id=? AND n.validee=true";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, etudiantId);
            ps.setLong(2, promotionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("moyenne") : 0.0;
            }
        } catch (SQLException ex) {
            LOGGER.error("Erreur calcul moyenne", ex);
            return 0.0;
        }
    }

    private List<Note> findByQuery(String sql, Object... params) {
        List<Note> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(mapper(rs)); }
        } catch (SQLException ex) {
            LOGGER.error("Erreur recherche notes", ex);
            throw new DAOException("Erreur recherche notes: " + ex.getMessage());
        }
        return list;
    }

    private Note mapper(ResultSet rs) throws SQLException {
        Note n = new Note();
        n.setId(rs.getLong("id"));
        n.setValeur(rs.getDouble("valeur"));
        n.setTypeNote(Note.TypeNote.valueOf(rs.getString("type_note")));
        n.setEtudiantId(rs.getLong("etudiant_id"));
        n.setSousModuleId(rs.getLong("sous_module_id"));
        long sp = rs.getLong("saisi_par");
        if (!rs.wasNull()) n.setSaisiPar(sp);
        n.setValidee(rs.getBoolean("validee"));
        Timestamp ds = rs.getTimestamp("date_saisie");
        if (ds != null) n.setDateSaisie(ds.toLocalDateTime());
        try { n.setEtudiantNom(rs.getString("etudiant_nom")); } catch (SQLException ignored) {}
        try { n.setEtudiantCne(rs.getString("etudiant_cne")); } catch (SQLException ignored) {}
        try { n.setSousModuleIntitule(rs.getString("sous_module_intitule")); } catch (SQLException ignored) {}
        try { n.setModuleIntitule(rs.getString("module_intitule")); } catch (SQLException ignored) {}
        return n;
    }
}
