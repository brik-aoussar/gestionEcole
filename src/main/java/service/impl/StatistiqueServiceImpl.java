package service.impl;

import config.Constantes;
import dao.EtudiantDAO;
import dao.ModuleDAO;
import dao.NoteDAO;
import dao.PromotionDAO;
import dao.StatistiqueDAO;
import exception.ServiceException;
import model.Etudiant;
import model.Promotion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.StatistiqueService;

import java.util.*;

public class StatistiqueServiceImpl implements StatistiqueService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatistiqueServiceImpl.class);
    private final StatistiqueDAO statistiqueDAO;
    private final EtudiantDAO etudiantDAO;
    private final NoteDAO noteDAO;
    private final ModuleDAO moduleDAO;
    private final PromotionDAO promotionDAO;

    public StatistiqueServiceImpl(StatistiqueDAO statistiqueDAO, EtudiantDAO etudiantDAO,
                                   NoteDAO noteDAO, ModuleDAO moduleDAO, PromotionDAO promotionDAO) {
        this.statistiqueDAO = statistiqueDAO;
        this.etudiantDAO = etudiantDAO;
        this.noteDAO = noteDAO;
        this.moduleDAO = moduleDAO;
        this.promotionDAO = promotionDAO;
    }

    @Override
    public double calculerMoyenneGenerale(Long etudiantId, Long promotionId) {
        return noteDAO.calculerMoyenneGenerale(etudiantId, promotionId);
    }

    @Override
    public List<Map<String, Object>> getClassement(Long promotionId) {
        if (promotionDAO.findById(promotionId) == null) throw new ServiceException("Promotion introuvable");
        return statistiqueDAO.getClassementPromotion(promotionId);
    }

    @Override
    public double getTauxReussite(Long promotionId) {
        if (promotionDAO.findById(promotionId) == null) throw new ServiceException("Promotion introuvable");
        return statistiqueDAO.getTauxReussite(promotionId);
    }

    @Override
    public List<Map<String, Object>> getEtudiantsEnEchec(Long promotionId) {
        if (promotionDAO.findById(promotionId) == null) throw new ServiceException("Promotion introuvable");
        return statistiqueDAO.getEtudiantsEnEchec(promotionId);
    }

    @Override
    public List<Map<String, Object>> getRapportParPromotion(Long promotionId) {
        if (promotionDAO.findById(promotionId) == null) throw new ServiceException("Promotion introuvable");
        List<Map<String, Object>> rapport = new ArrayList<>();
        rapport.addAll(statistiqueDAO.getClassementPromotion(promotionId));
        return rapport;
    }

    @Override
    public List<Map<String, Object>> getRapportParFiliere(Long filiereId) {
        List<Map<String, Object>> rapport = new ArrayList<>();
        List<Promotion> promotions = promotionDAO.findByFiliere(filiereId);
        for (Promotion p : promotions) {
            List<Map<String, Object>> classement = statistiqueDAO.getClassementPromotion(p.getId());
            for (Map<String, Object> row : classement) {
                row.put("promotion", p.getIntitule());
                rapport.add(row);
            }
        }
        return rapport;
    }

    @Override
    public Map<String, Object> getMeilleurEtudiant(Long promotionId) {
        List<Map<String, Object>> classement = getClassement(promotionId);
        if (classement.isEmpty()) return Collections.emptyMap();
        return classement.get(0);
    }

    @Override
    public Map<String, Object> getFicheEtudiant(Long etudiantId, Long promotionId) {
        Etudiant e = etudiantDAO.findById(etudiantId);
        if (e == null) throw new ServiceException("Etudiant introuvable");
        Map<String, Object> fiche = new HashMap<>();
        fiche.put("etudiant", e);
        fiche.put("moyenneGenerale", calculerMoyenneGenerale(etudiantId, promotionId));
        fiche.put("notes", noteDAO.findByEtudiant(etudiantId));
        fiche.put("mention", calculerMention(calculerMoyenneGenerale(etudiantId, promotionId)));
        return fiche;
    }

    @Override
    public Map<String, Object> getStatistiquesGlobales() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEtudiants", etudiantDAO.countAll());
        stats.put("totalEtudiantsActifs", etudiantDAO.countByStatut(Etudiant.Statut.ACTIF));
        stats.put("totalEtudiantsArchives", etudiantDAO.countByStatut(Etudiant.Statut.ARCHIVE));
        stats.put("totalPromotions", promotionDAO.countAll());
        stats.put("totalModules", moduleDAO.countModules());
        stats.put("totalSousModules", moduleDAO.countSousModules());
        return stats;
    }

    private String calculerMention(double moyenne) {
        if (moyenne >= Constantes.MENTION_TRES_BIEN) return "Tres Bien";
        if (moyenne >= Constantes.MENTION_BIEN) return "Bien";
        if (moyenne >= Constantes.MENTION_ASSEZ_BIEN) return "Assez Bien";
        if (moyenne >= Constantes.MENTION_PASSABLE) return "Passable";
        return "Non Valide";
    }
}
