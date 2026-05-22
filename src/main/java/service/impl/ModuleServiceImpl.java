package service.impl;

import dao.ModuleDAO;
import dao.PromotionDAO;
import exception.ServiceException;
import model.Module;
import model.SousModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.ModuleService;

import java.util.List;

public class ModuleServiceImpl implements ModuleService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleServiceImpl.class);
    private final ModuleDAO moduleDAO;
    private final PromotionDAO promotionDAO;

    public ModuleServiceImpl(ModuleDAO moduleDAO, PromotionDAO promotionDAO) {
        this.moduleDAO = moduleDAO;
        this.promotionDAO = promotionDAO;
    }

    @Override
    public Module ajouterModule(Module m) {
        if (m.getCode() == null || m.getCode().isBlank()) throw new ServiceException("Code module obligatoire");
        if (promotionDAO.findById(m.getPromotionId()) == null) throw new ServiceException("Promotion introuvable");
        LOGGER.info("Ajout module: {}", m.getCode());
        return moduleDAO.insertModule(m);
    }

    @Override
    public Module modifierModule(Module m) {
        if (moduleDAO.findModuleById(m.getId()) == null) throw new ServiceException("Module introuvable");
        LOGGER.info("Modification module: {}", m.getId());
        return moduleDAO.updateModule(m);
    }

    @Override
    public void supprimerModule(Long id) {
        if (moduleDAO.findModuleById(id) == null) throw new ServiceException("Module introuvable");
        moduleDAO.deleteModule(id);
        LOGGER.info("Suppression module: {}", id);
    }

    @Override
    public List<Module> recupererParPromotion(Long promotionId) {
        return moduleDAO.findByPromotion(promotionId);
    }

    @Override
    public Module recupererModuleParId(Long id) { return moduleDAO.findModuleById(id); }

    @Override
    public SousModule ajouterSousModule(SousModule sm) {
        if (sm.getCode() == null || sm.getCode().isBlank()) throw new ServiceException("Code sous-module obligatoire");
        if (moduleDAO.findModuleById(sm.getModuleId()) == null) throw new ServiceException("Module introuvable");
        LOGGER.info("Ajout sous-module: {}", sm.getCode());
        return moduleDAO.insertSousModule(sm);
    }

    @Override
    public SousModule modifierSousModule(SousModule sm) {
        if (moduleDAO.findSousModuleById(sm.getId()) == null) throw new ServiceException("Sous-module introuvable");
        LOGGER.info("Modification sous-module: {}", sm.getId());
        return moduleDAO.updateSousModule(sm);
    }

    @Override
    public void supprimerSousModule(Long id) {
        if (moduleDAO.findSousModuleById(id) == null) throw new ServiceException("Sous-module introuvable");
        moduleDAO.deleteSousModule(id);
        LOGGER.info("Suppression sous-module: {}", id);
    }

    @Override
    public List<SousModule> recupererSousModulesParModule(Long moduleId) {
        return moduleDAO.findSousModulesByModule(moduleId);
    }

    @Override
    public List<SousModule> recupererParEnseignant(Long enseignantId) {
        return moduleDAO.findByEnseignant(enseignantId);
    }

    @Override
    public void assignerEnseignant(Long sousModuleId, Long enseignantId) {
        if (moduleDAO.findSousModuleById(sousModuleId) == null) throw new ServiceException("Sous-module introuvable");
        moduleDAO.assignerEnseignant(sousModuleId, enseignantId);
        LOGGER.info("Assignation enseignant {} au sous-module {}", enseignantId, sousModuleId);
    }

    @Override
    public SousModule recupererSousModuleParId(Long id) { return moduleDAO.findSousModuleById(id); }
}
