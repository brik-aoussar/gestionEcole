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
import util.Validator;

import java.util.List;

/**
 * Service de gestion des etudiants.
 * FIXED: validation complete via Validator.validerEtudiant(),
 *        gestion des doublons CNE, inscriptions robustes.
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
    public Etudiant ajouterEtudiant(Etudiant e) {
        String erreur = Validator.validerEtudiant(e);
        if (erreur != null) throw new ValidationException("etudiant", erreur);

        if (etudiantDAO.findByCne(e.getCne()) != null) {
            throw new ServiceException("CNE deja existant: " + e.getCne());
        }
        LOGGER.info("Ajout etudiant: {}", e.getCne());
        return etudiantDAO.insert(e);
    }

    @Override
    public Etudiant modifierEtudiant(Etudiant e) {
        String erreur = Validator.validerEtudiant(e);
        if (erreur != null) throw new ValidationException("etudiant", erreur);

        Etudiant existant = etudiantDAO.findById(e.getId());
        if (existant == null) throw new ServiceException("Etudiant introuvable");

        // Verifier que le nouveau CNE n'est pas deja utilise par un autre etudiant
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
    public void supprimerEtudiant(Long id) {
        if (etudiantDAO.findById(id) == null) throw new ServiceException("Etudiant introuvable");
        etudiantDAO.delete(id);
        LOGGER.info("Suppression etudiant: {}", id);
    }

    @Override
    public void archiverEtudiant(Long id) {
        Etudiant e = etudiantDAO.findById(id);
        if (e == null) throw new ServiceException("Etudiant introuvable");
        e.setStatut(Etudiant.Statut.ARCHIVE);
        etudiantDAO.update(e);
        LOGGER.info("Archivage etudiant: {}", id);
    }

    @Override
    public void inscrireEtudiant(Long etudiantId, Long promotionId) {
        Etudiant e = etudiantDAO.findById(etudiantId);
        if (e == null) throw new ServiceException("Etudiant introuvable");
        Promotion p = promotionDAO.findById(promotionId);
        if (p == null) throw new ServiceException("Promotion introuvable");
        etudiantDAO.inscrire(etudiantId, promotionId);
        LOGGER.info("Inscription etudiant {} a promotion {}", etudiantId, promotionId);
    }

    @Override
    public Etudiant getEtudiant(Long id) { return etudiantDAO.findById(id); }

    @Override
    public Etudiant getEtudiantParCne(String cne) { return etudiantDAO.findByCne(cne); }

    @Override
    public List<Etudiant> listerEtudiants() { return etudiantDAO.findAll(); }

    @Override
    public List<Etudiant> listerEtudiantsParPromotion(Long promotionId) {
        return etudiantDAO.findByPromotion(promotionId);
    }

    @Override
    public List<Etudiant> rechercherEtudiants(String terme) { return etudiantDAO.search(terme); }
}
