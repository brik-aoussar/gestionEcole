package service;

import model.Etudiant;
import java.io.File;
import java.util.List;

/**
 * Interface du service Etudiant.
 * FIXED: toutes les methodes utilisees par les controleurs.
 */
public interface EtudiantService {
    Etudiant ajouter(Etudiant e);
    Etudiant modifier(Etudiant e);
    void supprimer(Long id);
    Etudiant recupererParId(Long id);
    Etudiant recupererParCne(String cne);
    List<Etudiant> recupererTous();
    List<Etudiant> recupererParPromotion(Long promotionId);
    List<Etudiant> recupererParFiliere(Long filiereId);
    List<Etudiant> rechercher(String terme);
    void archiver(Long id);
    void inscrireAPromotion(Long etudiantId, Long promotionId);
    int importerDepuisExcel(File fichier, Long promotionId);
}
