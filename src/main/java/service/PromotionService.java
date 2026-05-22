package service;

import model.Promotion;
import java.util.List;

public interface PromotionService {
    Promotion ajouter(Promotion p);
    Promotion modifier(Promotion p);
    void supprimer(Long id);
    List<Promotion> recupererToutes();
    Promotion recupererParId(Long id);
    List<Promotion> recupererParFiliere(Long filiereId);
    List<Promotion> recupererActives();
}
