package service.impl;

import config.Constantes;
import dao.EtudiantDAO;
import dao.PromotionDAO;
import exception.ServiceException;
import exception.ValidationException;
import model.Etudiant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.EtudiantService;
import util.ExcelImporter;
import util.Validator;

import java.io.File;
import java.util.List;

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
        valider(e);
        if (etudiantDAO.findByCne(e.getCne()) != null) {
            throw new ValidationException("cne", "CNE deja existant: " + e.getCne());
        }
        LOGGER.info("Ajout etudiant: {}", e.getCne());
        return etudiantDAO.insert(e);
    }

    @Override
    public Etudiant modifier(Etudiant e) {
        valider(e);
        if (etudiantDAO.findById(e.getId()) == null) throw new ServiceException("Etudiant introuvable");
        LOGGER.info("Modification etudiant: {}", e.getCne());
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
    public List<Etudiant> recupererTous() { return etudiantDAO.findAll(); }

    @Override
    public List<Etudiant> recupererParPromotion(Long promotionId) {
        return etudiantDAO.findByPromotion(promotionId);
    }

    @Override
    public List<Etudiant> recupererParFiliere(Long filiereId) {
        return etudiantDAO.findByFiliere(filiereId);
    }

    @Override
    public List<Etudiant> rechercher(String terme) { return etudiantDAO.search(terme); }

    @Override
    public int importerDepuisExcel(File fichier, Long promotionId) {
        if (promotionId == null) throw new ValidationException("promotion", "Promotion obligatoire");
        if (promotionDAO.findById(promotionId) == null) throw new ServiceException("Promotion introuvable");
        List<Etudiant> etudiants = ExcelImporter.importerEtudiants(fichier);
        int count = etudiantDAO.insertBatch(etudiants);
        for (Etudiant e : etudiants) {
            Etudiant saved = etudiantDAO.findByCne(e.getCne());
            if (saved != null) etudiantDAO.inscrire(saved.getId(), promotionId);
        }
        LOGGER.info("Import de {} etudiants dans promotion {}", count, promotionId);
        return count;
    }

    @Override
    public void inscrireAPromotion(Long etudiantId, Long promotionId) {
        if (etudiantDAO.findById(etudiantId) == null) throw new ServiceException("Etudiant introuvable");
        if (promotionDAO.findById(promotionId) == null) throw new ServiceException("Promotion introuvable");
        etudiantDAO.inscrire(etudiantId, promotionId);
        LOGGER.info("Inscription etudiant {} a promotion {}", etudiantId, promotionId);
    }

    @Override
    public Etudiant recupererParId(Long id) { return etudiantDAO.findById(id); }

    @Override
    public Etudiant recupererParCne(String cne) { return etudiantDAO.findByCne(cne); }

    private void valider(Etudiant e) {
        if (e.getCne() == null || !Validator.validerCne(e.getCne()))
            throw new ValidationException("cne", Constantes.MSG_CNE_INVALIDE);
        if (e.getNom() == null || e.getNom().isBlank())
            throw new ValidationException("nom", Constantes.MSG_CHAMP_OBLIGATOIRE);
        if (e.getPrenom() == null || e.getPrenom().isBlank())
            throw new ValidationException("prenom", Constantes.MSG_CHAMP_OBLIGATOIRE);
        if (e.getEmail() != null && !e.getEmail().isBlank() && !Validator.validerEmail(e.getEmail()))
            throw new ValidationException("email", Constantes.MSG_EMAIL_INVALIDE);
        if (e.getTelephone() != null && !e.getTelephone().isBlank() && !Validator.validerTelephone(e.getTelephone()))
            throw new ValidationException("telephone", Constantes.MSG_TELEPHONE_INVALIDE);
    }
}
