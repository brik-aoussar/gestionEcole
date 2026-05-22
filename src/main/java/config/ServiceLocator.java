package config;

import dao.*;
import service.*;
import service.impl.*;

/**
 * Assemblage manuel de toutes les dependances (DAO -> Service).
 * Pattern Service Locator / Manual DI Container.
 * FIXED: singleton thread-safe + constructeur public pour compatibilite legacy.
 */
public class ServiceLocator {

    // ── Singleton instance ──────────────────────────────────────────────────
    private static volatile ServiceLocator INSTANCE;
    private static final Object LOCK = new Object();

    // ── DAO ───────────────────────────────────────────────────────────────────
    private final FiliereDAO filiereDAO;
    private final PromotionDAO promotionDAO;
    private final EtudiantDAO etudiantDAO;
    private final ModuleDAO moduleDAO;
    private final NoteDAO noteDAO;
    private final UtilisateurDAO utilisateurDAO;
    private final StatistiqueDAO statistiqueDAO;

    // ── SERVICES ──────────────────────────────────────────────────────────────
    private final AuthService authService;
    private final FiliereService filiereService;
    private final EtudiantService etudiantService;
    private final ModuleService moduleService;
    private final NoteService noteService;
    private final StatistiqueService statistiqueService;
    private final PromotionService promotionService;

    /**
     * FIXED: constructeur public pour compatibilite avec le code existant
     * qui fait "new ServiceLocator()" dans les controleurs.
     * Mais en interne, on delegue au singleton pour eviter la recreation des DAO.
     */
    public ServiceLocator() {
        // Si le singleton existe deja, on recupere ses references
        if (INSTANCE != null) {
            this.filiereDAO = INSTANCE.filiereDAO;
            this.promotionDAO = INSTANCE.promotionDAO;
            this.etudiantDAO = INSTANCE.etudiantDAO;
            this.moduleDAO = INSTANCE.moduleDAO;
            this.noteDAO = INSTANCE.noteDAO;
            this.utilisateurDAO = INSTANCE.utilisateurDAO;
            this.statistiqueDAO = INSTANCE.statistiqueDAO;
            this.authService = INSTANCE.authService;
            this.filiereService = INSTANCE.filiereService;
            this.etudiantService = INSTANCE.etudiantService;
            this.moduleService = INSTANCE.moduleService;
            this.noteService = INSTANCE.noteService;
            this.statistiqueService = INSTANCE.statistiqueService;
            this.promotionService = INSTANCE.promotionService;
            return;
        }
        // Sinon on initialise (premier appel)
        synchronized (LOCK) {
            if (INSTANCE == null) {
                this.filiereDAO = new FiliereDAO();
                this.promotionDAO = new PromotionDAO();
                this.etudiantDAO = new EtudiantDAO();
                this.moduleDAO = new ModuleDAO();
                this.noteDAO = new NoteDAO();
                this.utilisateurDAO = new UtilisateurDAO();
                this.statistiqueDAO = new StatistiqueDAO();

                this.authService = new AuthServiceImpl(utilisateurDAO);
                this.filiereService = new FiliereServiceImpl(filiereDAO);
                this.etudiantService = new EtudiantServiceImpl(etudiantDAO, promotionDAO);
                this.moduleService = new ModuleServiceImpl(moduleDAO, promotionDAO);
                this.noteService = new NoteServiceImpl(noteDAO, etudiantDAO, moduleDAO);
                this.statistiqueService = new StatistiqueServiceImpl(
                        statistiqueDAO, etudiantDAO, noteDAO, moduleDAO, promotionDAO);
                this.promotionService = new PromotionServiceImpl(promotionDAO, filiereDAO);

                INSTANCE = this;
            } else {
                // Double-checked: un autre thread a initialise entre-temps
                this.filiereDAO = INSTANCE.filiereDAO;
                this.promotionDAO = INSTANCE.promotionDAO;
                this.etudiantDAO = INSTANCE.etudiantDAO;
                this.moduleDAO = INSTANCE.moduleDAO;
                this.noteDAO = INSTANCE.noteDAO;
                this.utilisateurDAO = INSTANCE.utilisateurDAO;
                this.statistiqueDAO = INSTANCE.statistiqueDAO;
                this.authService = INSTANCE.authService;
                this.filiereService = INSTANCE.filiereService;
                this.etudiantService = INSTANCE.etudiantService;
                this.moduleService = INSTANCE.moduleService;
                this.noteService = INSTANCE.noteService;
                this.statistiqueService = INSTANCE.statistiqueService;
                this.promotionService = INSTANCE.promotionService;
            }
        }
    }

    public static ServiceLocator getInstance() {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                if (INSTANCE == null) {
                    new ServiceLocator();
                }
            }
        }
        return INSTANCE;
    }

    public AuthService getAuthService() { return authService; }
    public FiliereService getFiliereService() { return filiereService; }
    public EtudiantService getEtudiantService() { return etudiantService; }
    public ModuleService getModuleService() { return moduleService; }
    public NoteService getNoteService() { return noteService; }
    public StatistiqueService getStatistiqueService() { return statistiqueService; }
    public PromotionService getPromotionService() { return promotionService; }
}
