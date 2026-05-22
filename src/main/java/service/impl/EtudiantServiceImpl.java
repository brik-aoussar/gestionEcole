package service.impl;

import config.Constantes;
import dao.EtudiantDAO;
import dao.PromotionDAO;
import exception.ServiceException;
import exception.ValidationException;
import model.Etudiant;
import model.Promotion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.EtudiantService;
import util.ExcelImporter;
import util.Validator;

import java.io.File;
import java.util.List;

/**
 * Service de gestion des etudiants.
 * FIXED: noms des methodes alignes sur l'interface EtudiantService,
 *        ajout des methodes manquantes recupererParFiliere() et importerDepuisExcel().
 */
public class EtudiantServiceImpl implements EtudiantService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EtudiantServiceImpl.class);
    private final EtudiantDAO etudiantDAO;
    private final PromotionDAO promotionDAO;

    public EtudiantServiceImpl(EtudiantDAO etudiantDAO, PromotionDAO promotionDAO) {
        this.etudiantDAO = etudiantDAO;
        this.promotionDAO = promotionDAO;
    }

    @Override
    public Etudiant ajouter(Etudiant e) {
        String erreur = Validator.validerEtudiant(e);
        if (erreur != null) throw new ValidationException("etudiant", erreur);

        if (etudiantDAO.findByCne(e.getCne()) != null) {
            throw new ServiceException("CNE deja existant: " + e.getCne());
        }
        LOGGER.info("Ajout etudiant: {}", e.getCne());
        return etudiantDAO.insert(e);
    }

    @Override
    public Etudiant modifier(Etudiant e) {
        String erreur = Validator.validerEtudiant(e);
        if (erreur != null) throw new ValidationException("etudiant", erreur);

        Etudiant existant = etudiantDAO.findById(e.getId());
        if (existant == null) throw new ServiceException("Etudiant introuvable");

        if (!existant.getCne().equals(e.getCne())) {
            Etudiant doublon = etudiantDAO.findByCne(e.getCne());
            if (doublon != null && !doublon.getId().equals(e.getId())) {
                throw new ServiceException("CNE deja utilise par un autre etudiant");
            }
        }
        LOGGER.info("Modification etudiant: {}", e.getId());
        return etudiantDAO.update(e);
    }

    @Override
    public void archiver(Long id) {
        Etudiant e = etudiantDAO.findById(id);
        if (e == null) throw new ServiceException("Etudiant introuvable");
        e.setStatut(Etudiant.Statut.ARCHIVE);
        etudiantDAO.update(e);
        LOGGER.info("Archivage etudiant: {}", id);
    }

    @Override
    public List<Etudiant> recupererTous() {
        return etudiantDAO.findAll();
    }

    @Override
    public List<Etudiant> recupererParPromotion(Long promotionId) {
        return etudiantDAO.findByPromotion(promotionId);
    }

    @Override
    public List<Etudiant> recupererParFiliere(Long filiereId) {
        return etudiantDAO.findByFiliere(filiereId);
    }

    @Override
    public List<Etudiant> rechercher(String terme) {
        return etudiantDAO.search(terme);
    }

    /**
     * Importe les etudiants depuis un fichier Excel et les inscrit a une promotion.
     * FIXED: methode manquante dans l'implementation originale.
     */
    @Override
    public int importerDepuisExcel(File fichier, Long promotionId) {
        Promotion p = promotionDAO.findById(promotionId);
        if (p == null) throw new ServiceException("Promotion introuvable (id=" + promotionId + ")");

        List<Etudiant> etudiants = ExcelImporter.importerEtudiants(fichier);
        int count = etudiantDAO.insertBatch(etudiants);

        // Inscrire chaque etudiant dans la promotion
        for (Etudiant e : etudiants) {
            try {
                Etudiant saved = etudiantDAO.findByCne(e.getCne());
                if (saved != null) {
                    etudiantDAO.inscrire(saved.getId(), promotionId);
                }
            } catch (Exception ex) {
                LOGGER.warn("Erreur inscription etudiant CNE={}: {}", e.getCne(), ex.getMessage());
            }
        }
        LOGGER.info("Import Excel: {} etudiants importes, promotion={}", count, promotionId);
        return count;
    }

    @Override
    public void inscrireAPromotion(Long etudiantId, Long promotionId) {
        Etudiant e = etudiantDAO.findById(etudiantId);
        if (e == null) throw new ServiceException("Etudiant introuvable");
        Promotion p = promotionDAO.findById(promotionId);
        if (p == null) throw new ServiceException("Promotion introuvable");
        etudiantDAO.inscrire(etudiantId, promotionId);
        LOGGER.info("Inscription etudiant {} a promotion {}", etudiantId, promotionId);
    }

    @Override
    public Etudiant recupererParId(Long id) {
        return etudiantDAO.findById(id);
    }

    @Override
    public Etudiant recupererParCne(String cne) {
        return etudiantDAO.findByCne(cne);
    }

    /** Methode supplementaire non presente dans l'interface (utilisable en interne). */
    public void supprimer(Long id) {
        if (etudiantDAO.findById(id) == null) throw new ServiceException("Etudiant introuvable");
        etudiantDAO.delete(id);
        LOGGER.info("Suppression etudiant: {}", id);
    }
}
