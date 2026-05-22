package service;

import java.util.List;
import java.util.Map;

public interface StatistiqueService {
    double calculerMoyenneGenerale(Long etudiantId, Long promotionId);
    List<Map<String, Object>> getClassement(Long promotionId);
    double getTauxReussite(Long promotionId);
    List<Map<String, Object>> getEtudiantsEnEchec(Long promotionId);
    List<Map<String, Object>> getRapportParPromotion(Long promotionId);
    List<Map<String, Object>> getRapportParFiliere(Long filiereId);
    Map<String, Object> getMeilleurEtudiant(Long promotionId);
    Map<String, Object> getFicheEtudiant(Long etudiantId, Long promotionId);
    Map<String, Object> getStatistiquesGlobales();
}
