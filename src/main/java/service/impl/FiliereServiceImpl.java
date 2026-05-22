package service.impl;

import dao.FiliereDAO;
import exception.ServiceException;
import model.Filiere;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.FiliereService;

import java.util.List;

public class FiliereServiceImpl implements FiliereService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FiliereServiceImpl.class);
    private final FiliereDAO filiereDAO;

    public FiliereServiceImpl(FiliereDAO filiereDAO) {
        this.filiereDAO = filiereDAO;
    }

    @Override
    public Filiere ajouter(Filiere f) {
        if (f.getCode() == null || f.getCode().isBlank()) throw new ServiceException("Code filiere obligatoire");
        if (f.getIntitule() == null || f.getIntitule().isBlank()) throw new ServiceException("Intitule filiere obligatoire");
        LOGGER.info("Ajout filiere: {}", f.getCode());
        return filiereDAO.insert(f);
    }

    @Override
    public Filiere modifier(Filiere f) {
        if (filiereDAO.findById(f.getId()) == null) throw new ServiceException("Filiere introuvable");
        LOGGER.info("Modification filiere: {}", f.getId());
        return filiereDAO.update(f);
    }

    @Override
    public void supprimer(Long id) {
        if (filiereDAO.findById(id) == null) throw new ServiceException("Filiere introuvable");
        filiereDAO.delete(id);
        LOGGER.info("Suppression filiere: {}", id);
    }

    @Override
    public List<Filiere> recupererToutes() { return filiereDAO.findAll(); }

    @Override
    public Filiere recupererParId(Long id) { return filiereDAO.findById(id); }

    @Override
    public List<Filiere> recupererParResponsable(Long responsableFiliereId) {
        return filiereDAO.findByResponsable(responsableFiliereId);
    }
}
