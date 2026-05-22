package service;

import model.Module;
import model.SousModule;
import java.util.List;

public interface ModuleService {
    Module ajouterModule(Module m);
    Module modifierModule(Module m);
    void supprimerModule(Long id);
    Module recupererModuleParId(Long id);
    List<Module> recupererParPromotion(Long promotionId);
    List<Module> recupererTous();

    SousModule ajouterSousModule(SousModule sm);
    SousModule modifierSousModule(SousModule sm);
    void supprimerSousModule(Long id);
    SousModule recupererSousModuleParId(Long id);
    List<SousModule> recupererSousModulesParModule(Long moduleId);
    List<SousModule> recupererParEnseignant(Long enseignantId);
    void assignerEnseignant(Long sousModuleId, Long enseignantId);
}
