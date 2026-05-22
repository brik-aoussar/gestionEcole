package service.impl;

import dao.FiliereDAO;
import dao.PromotionDAO;
import exception.ServiceException;
import model.Promotion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.PromotionService;

import java.util.List;

public class PromotionServiceImpl implements PromotionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PromotionServiceImpl.class);
    private final PromotionDAO promotionDAO;
    private final FiliereDAO filiereDAO;

    public PromotionServiceImpl(PromotionDAO promotionDAO, FiliereDAO filiereDAO) {
        this.promotionDAO = promotionDAO;
        this.filiereDAO = filiereDAO;
    }

    @Override
    public Promotion ajouter(Promotion p) {
        if (p.getIntitule() == null || p.getIntitule().isBlank()) throw new ServiceException("Intitule obligatoire");
        if (filiereDAO.findById(p.getFiliereId()) == null) throw new ServiceException("Filiere introuvable");
        LOGGER.info("Ajout promotion: {}", p.getIntitule());
        return promotionDAO.insert(p);
    }

    @Override
    public Promotion modifier(Promotion p) {
        if (promotionDAO.findById(p.getId()) == null) throw new ServiceException("Promotion introuvable");
        LOGGER.info("Modification promotion: {}", p.getId());
        return promotionDAO.update(p);
    }

    @Override
    public void supprimer(Long id) {
        if (promotionDAO.findById(id) == null) throw new ServiceException("Promotion introuvable");
        promotionDAO.delete(id);
        LOGGER.info("Suppression promotion: {}", id);
    }

    @Override
    public List<Promotion> recupererToutes() { return promotionDAO.findAll(); }

    @Override
    public Promotion recupererParId(Long id) { return promotionDAO.findById(id); }

    @Override
    public List<Promotion> recupererParFiliere(Long filiereId) { return promotionDAO.findByFiliere(filiereId); }

    @Override
    public List<Promotion> recupererActives() { return promotionDAO.findActives(); }
}
