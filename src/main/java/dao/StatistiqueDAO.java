package dao;

import config.DatabaseConnection;
import exception.DAOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

public class StatistiqueDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatistiqueDAO.class);

    public List<Map<String, Object>> getClassementPromotion(Long promotionId) {
        String sql = "SELECT e.cne, CONCAT(e.nom, ' ', e.prenom) as nom_complet, " +
                "ROUND(SUM(n.valeur * sm.coefficient) / SUM(sm.coefficient), 2) as moyenne_generale " +
                "FROM etudiant e " +
                "JOIN inscription i ON e.id = i.etudiant_id " +
                "JOIN note n ON e.id = n.etudiant_id " +
                "JOIN sous_module sm ON n.sous_module_id = sm.id " +
                "JOIN module m ON sm.module_id = m.id " +
                "WHERE i.promotion_id = ? AND m.promotion_id = ? AND n.validee = true " +
                "GROUP BY e.id, e.cne, e.nom, e.prenom " +
                "ORDER BY moyenne_generale DESC";
        return executeQuery(sql, promotionId, promotionId);
    }

    public double getTauxReussite(Long promotionId) {
        String sql = "SELECT COUNT(DISTINCT e.id) as total, " +
                "SUM(CASE WHEN (SELECT ROUND(SUM(n2.valeur * sm2.coefficient) / SUM(sm2.coefficient), 2) " +
                "FROM note n2 JOIN sous_module sm2 ON n2.sous_module_id=sm2.id JOIN module m2 ON sm2.module_id=m2.id " +
                "WHERE n2.etudiant_id=e.id AND m2.promotion_id=? AND n2.validee=true) >= 10 THEN 1 ELSE 0 END) as reussis " +
                "FROM etudiant e JOIN inscription i ON e.id=i.etudiant_id WHERE i.promotion_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, promotionId);
            ps.setLong(2, promotionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int reussis = rs.getInt("reussis");
                    return total > 0 ? (reussis * 100.0 / total) : 0.0;
                }
            }
        } catch (SQLException ex) {
            LOGGER.error("Erreur taux reussite", ex);
        }
        return 0.0;
    }

    public List<Map<String, Object>> getEtudiantsEnEchec(Long promotionId) {
        String sql = "SELECT e.cne, CONCAT(e.nom, ' ', e.prenom) as nom_complet, " +
                "ROUND(SUM(n.valeur * sm.coefficient) / SUM(sm.coefficient), 2) as moyenne_generale " +
                "FROM etudiant e " +
                "JOIN inscription i ON e.id = i.etudiant_id " +
                "JOIN note n ON e.id = n.etudiant_id " +
                "JOIN sous_module sm ON n.sous_module_id = sm.id " +
                "JOIN module m ON sm.module_id = m.id " +
                "WHERE i.promotion_id = ? AND m.promotion_id = ? AND n.validee = true " +
                "GROUP BY e.id, e.cne, e.nom, e.prenom " +
                "HAVING moyenne_generale < 10 " +
                "ORDER BY moyenne_generale ASC";
        return executeQuery(sql, promotionId, promotionId);
    }

    private List<Map<String, Object>> executeQuery(String sql, Object... params) {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= cols; i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    list.add(row);
                }
            }
        } catch (SQLException ex) {
            LOGGER.error("Erreur statistiques", ex);
            throw new DAOException("Erreur statistiques: " + ex.getMessage());
        }
        return list;
    }
}
