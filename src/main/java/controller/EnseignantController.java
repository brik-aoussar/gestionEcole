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
import javafx.stage.Stage;
import model.Enseignant;
import model.Etudiant;
import model.Module;
import model.Note;
import model.SousModule;
import model.Utilisateur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.EtudiantService;
import service.ModuleService;
import service.NoteService;
import util.FxUtils;

import java.io.File;
import java.io.IOException;

public class EnseignantController implements DashboardController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnseignantController.class);
    private Utilisateur utilisateur;
    private Enseignant enseignant;
    private final ServiceLocator locator = ServiceLocator.getInstance();
    private final ModuleService moduleService = locator.getModuleService();
    private final EtudiantService etudiantService = locator.getEtudiantService();
    private final NoteService noteService = locator.getNoteService();

    @FXML private Label lblNomEnseignant;
    @FXML private ComboBox<SousModule> cbSousModules;
    @FXML private TableView<Etudiant> tblEtudiants;
    @FXML private TableColumn<Etudiant, String> colCne, colNom, colPrenom;
    @FXML private TableColumn<Etudiant, String> colNote;  // FIXED: was Number, now String
    @FXML private TextField txtNote;
    @FXML private ComboBox<Note.TypeNote> cbTypeNote;
    @FXML private TableView<Note> tblNotes;
    @FXML private Label lblMoyenneClasse;

    private ObservableList<SousModule> sousModulesData = FXCollections.observableArrayList();
    private ObservableList<Etudiant> etudiantsData = FXCollections.observableArrayList();
    private ObservableList<Note> notesData = FXCollections.observableArrayList();

    @Override
    public void setUtilisateur(Utilisateur u) {
        this.utilisateur = u;
        this.enseignant = (Enseignant) u;
        lblNomEnseignant.setText(enseignant.getNomComplet());
        loadSousModules();
    }

    @FXML
    public void initialize() {
        cbTypeNote.setItems(FXCollections.observableArrayList(Note.TypeNote.values()));
        cbTypeNote.setValue(Note.TypeNote.EXAMEN);

        colCne.setCellValueFactory(new PropertyValueFactory<>("cne"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));

        // FIXED: colNote is now String type, returns formatted note or "-"
        colNote.setCellValueFactory(cell -> {
            Etudiant e = cell.getValue();
            SousModule sm = cbSousModules.getValue();
            if (sm == null) return new SimpleStringProperty("-");
            double note = getNoteForEtudiantSousModule(e.getId(), sm.getId());
            return new SimpleStringProperty(note >= 0 ? String.format("%.2f", note) : "-");
        });
        tblEtudiants.setItems(etudiantsData);

        tblNotes.getColumns().clear();
        TableColumn<Note, String> colNoteEtudiant = new TableColumn<>("Etudiant");
        colNoteEtudiant.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEtudiantNom()));
        TableColumn<Note, Number> colNoteValeur = new TableColumn<>("Note");
        colNoteValeur.setCellValueFactory(new PropertyValueFactory<>("valeur"));
        TableColumn<Note, String> colNoteType = new TableColumn<>("Type");
        colNoteType.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTypeNote().name()));
        tblNotes.getColumns().addAll(colNoteEtudiant, colNoteValeur, colNoteType);
        tblNotes.setItems(notesData);

        cbSousModules.setOnAction(e -> loadEtudiantsForSousModule());
    }

    private void loadSousModules() {
        sousModulesData.setAll(moduleService.recupererParEnseignant(enseignant.getId()));
        cbSousModules.setItems(sousModulesData);
    }

    private void loadEtudiantsForSousModule() {
        SousModule sm = cbSousModules.getValue();
        if (sm == null) return;

        Module m = moduleService.recupererModuleParId(sm.getModuleId());
        if (m != null) {
            etudiantsData.setAll(etudiantService.recupererParPromotion(m.getPromotionId()));
            notesData.setAll(noteService.getNotesParSousModule(sm.getId()));
            updateMoyenneClasse();
        }
    }

    private double getNoteForEtudiantSousModule(Long etudiantId, Long sousModuleId) {
        return notesData.stream()
                .filter(n -> n.getEtudiantId().equals(etudiantId) && n.getSousModuleId().equals(sousModuleId))
                .findFirst()
                .map(Note::getValeur)
                .orElse(-1.0);
    }

    private void updateMoyenneClasse() {
        double avg = notesData.stream().mapToDouble(Note::getValeur).average().orElse(0.0);
        lblMoyenneClasse.setText(String.format("Moyenne classe: %.2f/20", avg));
    }

    @FXML
    private void handleSaisirNote() {
        Etudiant e = tblEtudiants.getSelectionModel().getSelectedItem();
        SousModule sm = cbSousModules.getValue();
        String valStr = txtNote.getText().trim();
        Note.TypeNote type = cbTypeNote.getValue();

        if (e == null || sm == null || valStr.isEmpty() || type == null) {
            FxUtils.afficherAvertissement("Validation", "Veuillez selectionner un etudiant et saisir une note");
            return;
        }
        try {
            double val = Double.parseDouble(valStr);
            Note n = new Note(val, type, e.getId(), sm.getId(), enseignant.getEnseignantPk());
            noteService.saisirNote(n);
            loadEtudiantsForSousModule();
            txtNote.clear();
            FxUtils.afficherInfo("Succes", "Note saisie avec succes");
        } catch (NumberFormatException ex) {
            FxUtils.afficherErreur("Erreur", "Valeur de note invalide");
        }
    }

    @FXML
    private void handleImporterNotes() {
        SousModule sm = cbSousModules.getValue();
        if (sm == null) {
            FxUtils.afficherAvertissement("Selection", "Veuillez selectionner un sous-module");
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Importer des notes");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx", "*.xls"));
        File file = fc.showOpenDialog(tblEtudiants.getScene().getWindow());
        if (file == null) return;

        try {
            int count = noteService.importerNotes(file, sm.getId(), utilisateur.getId());
            loadEtudiantsForSousModule();
            FxUtils.afficherInfo("Succes", count + " notes importees avec succes");
        } catch (Exception ex) {
            FxUtils.afficherErreur("Erreur", "Erreur import: " + ex.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            Stage stage = (Stage) lblNomEnseignant.getScene().getWindow();
            stage.setTitle("Connexion | " + Constantes.APP_NAME);
            stage.setScene(scene);
            stage.setMaximized(false);
            stage.centerOnScreen();
        } catch (IOException ex) {
            FxUtils.afficherErreur("Erreur", "Impossible de retourner a la page de connexion");
        }
    }
}
