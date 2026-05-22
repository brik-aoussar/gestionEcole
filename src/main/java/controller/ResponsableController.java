package controller;

import config.Constantes;
import config.ServiceLocator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Enseignant;
import model.Etudiant;
import model.Filiere;
import model.Module;
import model.Note;
import model.Promotion;
import model.SousModule;
import model.Utilisateur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.EtudiantService;
import service.FiliereService;
import service.ModuleService;
import service.NoteService;
import service.PromotionService;
import service.StatistiqueService;
import util.FxUtils;
import util.PDFExporter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ResponsableController implements DashboardController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponsableController.class);
    private Utilisateur utilisateur;
    private final ServiceLocator locator = new ServiceLocator();
    private final EtudiantService etudiantService = locator.getEtudiantService();
    private final FiliereService filiereService = locator.getFiliereService();
    private final PromotionService promotionService = locator.getPromotionService();
    private final ModuleService moduleService = locator.getModuleService();
    private final NoteService noteService = locator.getNoteService();
    private final StatistiqueService statService = locator.getStatistiqueService();

    // -- Tabs -----------------------------------------------------------------
    @FXML private TabPane tabPane;
    @FXML private Tab tabEtudiants, tabPromotions, tabModules, tabNotes, tabStats, tabRapports;

    // -- Etudiants ------------------------------------------------------------
    @FXML private TableView<Etudiant> tblEtudiants;
    @FXML private TableColumn<Etudiant, String> colCne, colNom, colPrenom, colEmail, colStatut, colPromotion;
    @FXML private TextField txtSearchEtudiant;
    @FXML private ComboBox<Promotion> cbFilterPromotion;
    @FXML private Label lblTotalEtudiants, lblActifs, lblArchives;

    // -- Promotions -----------------------------------------------------------
    @FXML private TableView<Promotion> tblPromotions;
    @FXML private TableColumn<Promotion, String> colPromoIntitule, colPromoAnnee, colPromoFiliere, colPromoEtudiants;
    @FXML private ComboBox<Filiere> cbPromoFiliere;
    @FXML private TextField txtPromoIntitule, txtPromoAnnee;

    // -- Modules --------------------------------------------------------------
    @FXML private TableView<Module> tblModules;
    @FXML private TableView<SousModule> tblSousModules;
    @FXML private ComboBox<Promotion> cbModulePromotion;
    @FXML private TextField txtModuleCode, txtModuleIntitule, txtModuleCoef;
    @FXML private TextField txtSousModuleCode, txtSousModuleIntitule, txtSousModuleCoef;
    @FXML private ComboBox<Module> cbSousModuleModule;
    @FXML private ComboBox<Enseignant> cbSousModuleEnseignant;

    // -- Notes ----------------------------------------------------------------
    @FXML private TableView<Note> tblNotes;
    @FXML private ComboBox<Promotion> cbNotePromotion;
    @FXML private ComboBox<Module> cbNoteModule;
    @FXML private ComboBox<SousModule> cbNoteSousModule;
    @FXML private ComboBox<Etudiant> cbNoteEtudiant;
    @FXML private TextField txtNoteValeur;
    @FXML private ComboBox<Note.TypeNote> cbNoteType;

    // -- Stats ----------------------------------------------------------------
    @FXML private Label lblStatTotalEtudiants, lblStatTotalPromos, lblStatTotalModules, lblStatTotalSousModules;
    @FXML private ComboBox<Promotion> cbStatPromotion;
    @FXML private TableView<Map<String, Object>> tblClassement;
    @FXML private Label lblTauxReussite, lblMeilleurEtudiant;

    // -- Rapports -------------------------------------------------------------
    @FXML private ComboBox<Promotion> cbRapportPromotion;
    @FXML private ComboBox<Filiere> cbRapportFiliere;
    @FXML private javafx.scene.layout.VBox vboxRapportResult;

    private ObservableList<Etudiant> etudiantsData = FXCollections.observableArrayList();
    private ObservableList<Promotion> promotionsData = FXCollections.observableArrayList();
    private ObservableList<Module> modulesData = FXCollections.observableArrayList();
    private ObservableList<SousModule> sousModulesData = FXCollections.observableArrayList();
    private ObservableList<Note> notesData = FXCollections.observableArrayList();

    @Override
    public void setUtilisateur(Utilisateur u) { this.utilisateur = u; }

    @FXML
    public void initialize() {
        setupEtudiantsTab();
        setupPromotionsTab();
        setupModulesTab();
        setupNotesTab();
        setupStatsTab();
        setupRapportsTab();
        loadDashboardStats();
    }

    // =========================================================================
    // ETUDIANTS
    // =========================================================================
    private void setupEtudiantsTab() {
        colCne.setCellValueFactory(new PropertyValueFactory<>("cne"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colPromotion.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPromotionIntitule() != null ? cell.getValue().getPromotionIntitule() : "Non inscrit"));

        tblEtudiants.setItems(etudiantsData);
        tblEtudiants.setRowFactory(tv -> {
            TableRow<Etudiant> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    openEtudiantDialog(row.getItem());
                }
            });
            return row;
        });

        cbFilterPromotion.setItems(FXCollections.observableArrayList(promotionService.recupererToutes()));
        cbFilterPromotion.setOnAction(e -> filterEtudiants());
        txtSearchEtudiant.textProperty().addListener((obs, old, val) -> filterEtudiants());

        refreshEtudiants();
    }

    private void filterEtudiants() {
        Promotion p = cbFilterPromotion.getValue();
        String terme = txtSearchEtudiant.getText().trim();
        List<Etudiant> list;
        if (p != null) {
            list = etudiantService.recupererParPromotion(p.getId());
        } else if (!terme.isEmpty()) {
            list = etudiantService.rechercher(terme);
        } else {
            list = etudiantService.recupererTous();
        }
        etudiantsData.setAll(list);
        updateEtudiantStats();
    }

    private void refreshEtudiants() {
        etudiantsData.setAll(etudiantService.recupererTous());
        updateEtudiantStats();
    }

    private void updateEtudiantStats() {
        long actifs = etudiantsData.stream().filter(Etudiant::isActif).count();
        long archives = etudiantsData.stream().filter(e -> e.getStatut() == Etudiant.Statut.ARCHIVE).count();
        lblTotalEtudiants.setText(String.valueOf(etudiantsData.size()));
        lblActifs.setText(String.valueOf(actifs));
        lblArchives.setText(String.valueOf(archives));
    }

    @FXML
    private void handleAjouterEtudiant() {
        openEtudiantDialog(null);
    }

    @FXML
    private void handleModifierEtudiant() {
        Etudiant selected = tblEtudiants.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.afficherAvertissement("Selection", "Veuillez selectionner un etudiant");
            return;
        }
        openEtudiantDialog(selected);
    }

    @FXML
    private void handleArchiverEtudiant() {
        Etudiant selected = tblEtudiants.getSelectionModel().getSelectedItem();
        if (selected == null) {
            FxUtils.afficherAvertissement("Selection", "Veuillez selectionner un etudiant");
            return;
        }
        if (FxUtils.confirmer("Archivage", Constantes.MSG_CONFIRMATION_ARCHIVAGE)) {
            etudiantService.archiver(selected.getId());
            refreshEtudiants();
            FxUtils.afficherInfo("Succes", "Etudiant archive avec succes");
        }
    }

    @FXML
    private void handleImporterEtudiants() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Importer des etudiants");
        fc.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Excel", "*.xlsx", "*.xls")
        );
        File file = fc.showOpenDialog(tblEtudiants.getScene().getWindow());
        if (file == null) return;

        Promotion p = cbFilterPromotion.getValue();
        if (p == null) {
            FxUtils.afficherErreur("Erreur", "Veuillez selectionner une promotion pour l'import");
            return;
        }

        try {
            int count = etudiantService.importerDepuisExcel(file, p.getId());
            refreshEtudiants();
            FxUtils.afficherInfo("Succes", count + " etudiants importes avec succes");
        } catch (Exception ex) {
            FxUtils.afficherErreur("Erreur", "Erreur lors de l'import: " + ex.getMessage());
        }
    }

    @FXML
    private void handleExporterListeEtudiants() {
        Promotion p = cbFilterPromotion.getValue();
        List<Etudiant> list = p != null ? etudiantService.recupererParPromotion(p.getId()) : etudiantsData;
        try {
            File f = PDFExporter.exporterListeEtudiants(list, p != null ? p.getIntitule() : "Tous");
            FxUtils.afficherInfo("Export", "Liste exportee vers: " + f.getAbsolutePath());
        } catch (Exception ex) {
            FxUtils.afficherErreur("Erreur", "Erreur export PDF: " + ex.getMessage());
        }
    }

    private void openEtudiantDialog(Etudiant etudiant) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dialog/EtudiantDialog.fxml"));
            Parent root = loader.load();
            EtudiantDialogController ctrl = loader.getController();
            ctrl.setEtudiant(etudiant);
            ctrl.setPromotions(promotionService.recupererToutes());

            Stage stage = new Stage();
            stage.setTitle(etudiant == null ? "Nouvel Etudiant" : "Modifier Etudiant");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tblEtudiants.getScene().getWindow());
            stage.showAndWait();

            if (ctrl.isSaved()) {
                if (etudiant == null) {
                    etudiantService.ajouter(ctrl.getEtudiant());
                } else {
                    etudiantService.modifier(ctrl.getEtudiant());
                }
                if (ctrl.getSelectedPromotion() != null) {
                    etudiantService.inscrireAPromotion(ctrl.getEtudiant().getId(), ctrl.getSelectedPromotion().getId());
                }
                refreshEtudiants();
            }
        } catch (IOException ex) {
            FxUtils.afficherErreur("Erreur", "Impossible d'ouvrir le dialogue");
        }
    }

    // =========================================================================
    // PROMOTIONS
    // =========================================================================
    private void setupPromotionsTab() {
        colPromoIntitule.setCellValueFactory(new PropertyValueFactory<>("intitule"));
        colPromoAnnee.setCellValueFactory(new PropertyValueFactory<>("annee"));
        colPromoFiliere.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getFiliereIntitule()));
        colPromoEtudiants.setCellValueFactory(new PropertyValueFactory<>("nombreEtudiants"));
        tblPromotions.setItems(promotionsData);
        refreshPromotions();

        cbPromoFiliere.setItems(FXCollections.observableArrayList(filiereService.recupererToutes()));
    }

    private void refreshPromotions() {
        promotionsData.setAll(promotionService.recupererToutes());
    }

    @FXML
    private void handleAjouterPromotion() {
        String intitule = txtPromoIntitule.getText().trim();
        String anneeStr = txtPromoAnnee.getText().trim();
        Filiere f = cbPromoFiliere.getValue();

        if (intitule.isEmpty() || anneeStr.isEmpty() || f == null) {
            FxUtils.afficherAvertissement("Validation", "Tous les champs sont obligatoires");
            return;
        }
        try {
            int annee = Integer.parseInt(anneeStr);
            Promotion p = new Promotion(intitule, annee, f.getId());
            promotionService.ajouter(p);
            refreshPromotions();
            txtPromoIntitule.clear();
            txtPromoAnnee.clear();
            cbPromoFiliere.setValue(null);
            FxUtils.afficherInfo("Succes", "Promotion ajoutee avec succes");
        } catch (NumberFormatException ex) {
            FxUtils.afficherErreur("Erreur", "Annee invalide");
        }
    }

    @FXML
    private void handleSupprimerPromotion() {
        Promotion p = tblPromotions.getSelectionModel().getSelectedItem();
        if (p == null) {
            FxUtils.afficherAvertissement("Selection", "Veuillez selectionner une promotion");
            return;
        }
        if (FxUtils.confirmer("Suppression", Constantes.MSG_CONFIRMATION_SUPPRESSION)) {
            promotionService.supprimer(p.getId());
            refreshPromotions();
        }
    }

    // =========================================================================
    // MODULES
    // =========================================================================
    private void setupModulesTab() {
        tblModules.getColumns().clear();
        TableColumn<Module, String> colModCode = new TableColumn<>("Code");
        colModCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        TableColumn<Module, String> colModIntitule = new TableColumn<>("Intitule");
        colModIntitule.setCellValueFactory(new PropertyValueFactory<>("intitule"));
        TableColumn<Module, Number> colModCoef = new TableColumn<>("Coef");
        colModCoef.setCellValueFactory(new PropertyValueFactory<>("coefficient"));
        tblModules.getColumns().addAll(colModCode, colModIntitule, colModCoef);
        tblModules.setItems(modulesData);

        tblSousModules.getColumns().clear();
        TableColumn<SousModule, String> colSmCode = new TableColumn<>("Code");
        colSmCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        TableColumn<SousModule, String> colSmIntitule = new TableColumn<>("Intitule");
        colSmIntitule.setCellValueFactory(new PropertyValueFactory<>("intitule"));
        TableColumn<SousModule, Number> colSmCoef = new TableColumn<>("Coef");
        colSmCoef.setCellValueFactory(new PropertyValueFactory<>("coefficient"));
        TableColumn<SousModule, String> colSmEns = new TableColumn<>("Enseignant");
        colSmEns.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEnseignantNom() != null ? cell.getValue().getEnseignantNom() : "Non assigne"));
        tblSousModules.getColumns().addAll(colSmCode, colSmIntitule, colSmCoef, colSmEns);
        tblSousModules.setItems(sousModulesData);

        cbModulePromotion.setItems(FXCollections.observableArrayList(promotionService.recupererToutes()));
        cbModulePromotion.setOnAction(e -> loadModulesForPromotion());

        cbSousModuleModule.setItems(modulesData);
    }

    private void loadModulesForPromotion() {
        Promotion p = cbModulePromotion.getValue();
        if (p == null) return;
        modulesData.setAll(moduleService.recupererParPromotion(p.getId()));
        cbSousModuleModule.setItems(modulesData);
    }

    @FXML
    private void handleAjouterModule() {
        Promotion p = cbModulePromotion.getValue();
        if (p == null) {
            FxUtils.afficherAvertissement("Selection", "Veuillez selectionner une promotion");
            return;
        }
        String code = txtModuleCode.getText().trim();
        String intitule = txtModuleIntitule.getText().trim();
        String coefStr = txtModuleCoef.getText().trim();
        if (code.isEmpty() || intitule.isEmpty() || coefStr.isEmpty()) {
            FxUtils.afficherAvertissement("Validation", "Tous les champs sont obligatoires");
            return;
        }
        try {
            double coef = Double.parseDouble(coefStr);
            Module m = new Module(code, intitule, coef, p.getId());
            moduleService.ajouterModule(m);
            loadModulesForPromotion();
            txtModuleCode.clear();
            txtModuleIntitule.clear();
            txtModuleCoef.clear();
            FxUtils.afficherInfo("Succes", "Module ajoute avec succes");
        } catch (NumberFormatException ex) {
            FxUtils.afficherErreur("Erreur", "Coefficient invalide");
        }
    }

    @FXML
    private void handleSupprimerModule() {
        Module m = tblModules.getSelectionModel().getSelectedItem();
        if (m == null) {
            FxUtils.afficherAvertissement("Selection", "Veuillez selectionner un module");
            return;
        }
        if (FxUtils.confirmer("Suppression", Constantes.MSG_CONFIRMATION_SUPPRESSION)) {
            moduleService.supprimerModule(m.getId());
            loadModulesForPromotion();
        }
    }

    @FXML
    private void handleAjouterSousModule() {
        Module m = cbSousModuleModule.getValue();
        if (m == null) {
            FxUtils.afficherAvertissement("Selection", "Veuillez selectionner un module");
            return;
        }
        String code = txtSousModuleCode.getText().trim();
        String intitule = txtSousModuleIntitule.getText().trim();
        String coefStr = txtSousModuleCoef.getText().trim();
        if (code.isEmpty() || intitule.isEmpty() || coefStr.isEmpty()) {
            FxUtils.afficherAvertissement("Validation", "Tous les champs sont obligatoires");
            return;
        }
        try {
            double coef = Double.parseDouble(coefStr);
            SousModule sm = new SousModule(code, intitule, coef, m.getId(), null);
            moduleService.ajouterSousModule(sm);
            loadSousModulesForModule();
            txtSousModuleCode.clear();
            txtSousModuleIntitule.clear();
            txtSousModuleCoef.clear();
            FxUtils.afficherInfo("Succes", "Sous-module ajoute avec succes");
        } catch (NumberFormatException ex) {
            FxUtils.afficherErreur("Erreur", "Coefficient invalide");
        }
    }

    @FXML
    private void handleLoadSousModules() {
        loadSousModulesForModule();
    }

    private void loadSousModulesForModule() {
        Module m = tblModules.getSelectionModel().getSelectedItem();
        if (m == null) return;
        sousModulesData.setAll(moduleService.recupererSousModulesParModule(m.getId()));
    }

    // =========================================================================
    // NOTES
    // =========================================================================
    private void setupNotesTab() {
        tblNotes.getColumns().clear();
        TableColumn<Note, String> colNoteEtudiant = new TableColumn<>("Etudiant");
        colNoteEtudiant.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEtudiantNom()));
        TableColumn<Note, Number> colNoteValeur = new TableColumn<>("Note");
        colNoteValeur.setCellValueFactory(new PropertyValueFactory<>("valeur"));
        TableColumn<Note, String> colNoteType = new TableColumn<>("Type");
        colNoteType.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTypeNote().name()));
        TableColumn<Note, String> colNoteModule = new TableColumn<>("Module");
        colNoteModule.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getModuleIntitule()));
        TableColumn<Note, String> colNoteSm = new TableColumn<>("Sous-Module");
        colNoteSm.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSousModuleIntitule()));
        tblNotes.getColumns().addAll(colNoteEtudiant, colNoteValeur, colNoteType, colNoteModule, colNoteSm);
        tblNotes.setItems(notesData);

        cbNotePromotion.setItems(FXCollections.observableArrayList(promotionService.recupererToutes()));
        cbNotePromotion.setOnAction(e -> {
            Promotion p = cbNotePromotion.getValue();
            if (p != null) {
                cbNoteModule.setItems(FXCollections.observableArrayList(moduleService.recupererParPromotion(p.getId())));
            }
        });

        cbNoteModule.setOnAction(e -> {
            Module m = cbNoteModule.getValue();
            if (m != null) {
                cbNoteSousModule.setItems(FXCollections.observableArrayList(moduleService.recupererSousModulesParModule(m.getId())));
            }
        });

        cbNoteSousModule.setOnAction(e -> {
            SousModule sm = cbNoteSousModule.getValue();
            if (sm != null) {
                Promotion p = cbNotePromotion.getValue();
                if (p != null) {
                    cbNoteEtudiant.setItems(FXCollections.observableArrayList(etudiantService.recupererParPromotion(p.getId())));
                }
                refreshNotesForSousModule(sm.getId());
            }
        });

        cbNoteType.setItems(FXCollections.observableArrayList(Note.TypeNote.values()));
    }

    private void refreshNotesForSousModule(Long sousModuleId) {
        notesData.setAll(noteService.getNotesParSousModule(sousModuleId));
    }

    @FXML
    private void handleSaisirNote() {
        Etudiant e = cbNoteEtudiant.getValue();
        SousModule sm = cbNoteSousModule.getValue();
        String valStr = txtNoteValeur.getText().trim();
        Note.TypeNote type = cbNoteType.getValue();

        if (e == null || sm == null || valStr.isEmpty() || type == null) {
            FxUtils.afficherAvertissement("Validation", "Tous les champs sont obligatoires");
            return;
        }
        try {
            double val = Double.parseDouble(valStr);
            Note n = new Note(val, type, e.getId(), sm.getId(), utilisateur.getId());
            noteService.saisirNote(n);
            refreshNotesForSousModule(sm.getId());
            txtNoteValeur.clear();
            FxUtils.afficherInfo("Succes", "Note saisie avec succes");
        } catch (NumberFormatException ex) {
            FxUtils.afficherErreur("Erreur", "Valeur de note invalide");
        }
    }

    @FXML
    private void handleValiderNotes() {
        SousModule sm = cbNoteSousModule.getValue();
        if (sm == null) {
            FxUtils.afficherAvertissement("Selection", "Veuillez selectionner un sous-module");
            return;
        }
        if (FxUtils.confirmer("Validation", "Confirmer la validation des notes de ce sous-module ?")) {
            noteService.validerNotesSousModule(sm.getId());
            refreshNotesForSousModule(sm.getId());
            FxUtils.afficherInfo("Succes", "Notes validees avec succes");
        }
    }

    // =========================================================================
    // STATISTIQUES
    // =========================================================================
    private void setupStatsTab() {
        cbStatPromotion.setItems(FXCollections.observableArrayList(promotionService.recupererToutes()));
        cbStatPromotion.setOnAction(e -> loadStatsForPromotion());
    }

    private void loadDashboardStats() {
        Map<String, Object> stats = statService.getStatistiquesGlobales();
        lblStatTotalEtudiants.setText(String.valueOf(stats.get("totalEtudiants")));
        lblStatTotalPromos.setText(String.valueOf(stats.get("totalPromotions")));
        lblStatTotalModules.setText(String.valueOf(stats.get("totalModules")));
        lblStatTotalSousModules.setText(String.valueOf(stats.get("totalSousModules")));
    }

    private void loadStatsForPromotion() {
        Promotion p = cbStatPromotion.getValue();
        if (p == null) return;

        double taux = statService.getTauxReussite(p.getId());
        lblTauxReussite.setText(String.format("%.1f%%", taux));

        Map<String, Object> meilleur = statService.getMeilleurEtudiant(p.getId());
        if (!meilleur.isEmpty()) {
            lblMeilleurEtudiant.setText(meilleur.get("nom_complet") + " (" + meilleur.get("moyenne_generale") + ")");
        }

        List<Map<String, Object>> classement = statService.getClassement(p.getId());
        ObservableList<Map<String, Object>> data = FXCollections.observableArrayList(classement);
        tblClassement.setItems(data);
        if (tblClassement.getColumns().isEmpty()) {
            TableColumn<Map<String, Object>, String> colRang = new TableColumn<>("Rang");
            colRang.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(tblClassement.getItems().indexOf(cell.getValue()) + 1)));
            TableColumn<Map<String, Object>, String> colCne = new TableColumn<>("CNE");
            colCne.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().get("cne"))));
            TableColumn<Map<String, Object>, String> colNom = new TableColumn<>("Nom");
            colNom.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().get("nom_complet"))));
            TableColumn<Map<String, Object>, String> colMoy = new TableColumn<>("Moyenne");
            colMoy.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().get("moyenne_generale"))));
            tblClassement.getColumns().addAll(colRang, colCne, colNom, colMoy);
        }
    }

    // =========================================================================
    // RAPPORTS
    // =========================================================================
    private void setupRapportsTab() {
        cbRapportPromotion.setItems(FXCollections.observableArrayList(promotionService.recupererToutes()));
        cbRapportFiliere.setItems(FXCollections.observableArrayList(filiereService.recupererToutes()));
    }

    @FXML
    private void handleGenererRapportPromotion() {
        Promotion p = cbRapportPromotion.getValue();
        if (p == null) {
            FxUtils.afficherAvertissement("Selection", "Veuillez selectionner une promotion");
            return;
        }
        try {
            List<Map<String, Object>> rapport = statService.getRapportParPromotion(p.getId());
            File f = PDFExporter.exporterRapportClassement(rapport, p.getIntitule());
            FxUtils.afficherInfo("Succes", "Rapport genere: " + f.getAbsolutePath());
        } catch (Exception ex) {
            FxUtils.afficherErreur("Erreur", "Erreur generation rapport: " + ex.getMessage());
        }
    }

    @FXML
    private void handleGenererRapportFiliere() {
        Filiere f = cbRapportFiliere.getValue();
        if (f == null) {
            FxUtils.afficherAvertissement("Selection", "Veuillez selectionner une filiere");
            return;
        }
        try {
            List<Map<String, Object>> rapport = statService.getRapportParFiliere(f.getId());
            FxUtils.afficherInfo("Succes", "Rapport filiere genere avec " + rapport.size() + " lignes");
        } catch (Exception ex) {
            FxUtils.afficherErreur("Erreur", "Erreur generation rapport: " + ex.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            Stage stage = (Stage) tabPane.getScene().getWindow();
            stage.setTitle("Connexion | " + Constantes.APP_NAME);
            stage.setScene(scene);
            stage.setMaximized(false);
            stage.centerOnScreen();
        } catch (IOException ex) {
            FxUtils.afficherErreur("Erreur", "Impossible de retourner a la page de connexion");
        }
    }
}
