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
import javafx.stage.Stage;
import model.Etudiant;
import model.Note;
import model.Promotion;
import model.Utilisateur;
import model.ResponsableFiliere;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.EtudiantService;
import service.NoteService;
import service.PromotionService;
import service.StatistiqueService;
import util.FxUtils;
import util.PDFExporter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ResponsableFiliereController implements DashboardController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponsableFiliereController.class);
    private Utilisateur utilisateur;
    private ResponsableFiliere responsable;
    private final ServiceLocator locator = new ServiceLocator();
    private final EtudiantService etudiantService = locator.getEtudiantService();
    private final PromotionService promotionService = locator.getPromotionService();
    private final NoteService noteService = locator.getNoteService();
    private final StatistiqueService statService = locator.getStatistiqueService();

    @FXML private Label lblNomResponsable;
    @FXML private ComboBox<Promotion> cbPromotions;
    @FXML private TableView<Etudiant> tblEtudiants;
    @FXML private TableColumn<Etudiant, String> colCne, colNom, colPrenom, colEmail;
    @FXML private TableView<Note> tblNotes;
    @FXML private TableView<Map<String, Object>> tblClassement;
    @FXML private Label lblTauxReussite;

    private ObservableList<Etudiant> etudiantsData = FXCollections.observableArrayList();
    private ObservableList<Note> notesData = FXCollections.observableArrayList();

    @Override
    public void setUtilisateur(Utilisateur u) {
        this.utilisateur = u;
        this.responsable = (ResponsableFiliere) u;
        lblNomResponsable.setText(responsable.getNomComplet());
        loadPromotions();
    }

    @FXML
    public void initialize() {
        colCne.setCellValueFactory(new PropertyValueFactory<>("cne"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        tblEtudiants.setItems(etudiantsData);

        tblNotes.getColumns().clear();
        TableColumn<Note, String> colNoteEtudiant = new TableColumn<>("Etudiant");
        colNoteEtudiant.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEtudiantNom()));
        TableColumn<Note, Number> colNoteValeur = new TableColumn<>("Note");
        colNoteValeur.setCellValueFactory(new PropertyValueFactory<>("valeur"));
        TableColumn<Note, String> colNoteModule = new TableColumn<>("Module");
        colNoteModule.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getModuleIntitule()));
        tblNotes.getColumns().addAll(colNoteEtudiant, colNoteValeur, colNoteModule);
        tblNotes.setItems(notesData);

        cbPromotions.setOnAction(e -> loadDataForPromotion());
    }

    private void loadPromotions() {
        if (responsable.getFiliereId() != null) {
            List<Promotion> promos = promotionService.recupererParFiliere(responsable.getFiliereId());
            cbPromotions.setItems(FXCollections.observableArrayList(promos));
        }
    }

    private void loadDataForPromotion() {
        Promotion p = cbPromotions.getValue();
        if (p == null) return;

        etudiantsData.setAll(etudiantService.recupererParPromotion(p.getId()));
        notesData.setAll(noteService.getNotesParPromotion(p.getId()));

        double taux = statService.getTauxReussite(p.getId());
        lblTauxReussite.setText(String.format("%.1f%%", taux));

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

    @FXML
    private void handleExporterRapport() {
        Promotion p = cbPromotions.getValue();
        if (p == null) {
            FxUtils.afficherAvertissement("Selection", "Veuillez selectionner une promotion");
            return;
        }
        try {
            List<Map<String, Object>> classement = statService.getClassement(p.getId());
            File f = PDFExporter.exporterRapportClassement(classement, p.getIntitule());
            FxUtils.afficherInfo("Succes", "Rapport exporte: " + f.getAbsolutePath());
        } catch (Exception ex) {
            FxUtils.afficherErreur("Erreur", "Erreur export: " + ex.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            Stage stage = (Stage) lblNomResponsable.getScene().getWindow();
            stage.setTitle("Connexion | " + Constantes.APP_NAME);
            stage.setScene(scene);
            stage.setMaximized(false);
            stage.centerOnScreen();
        } catch (IOException ex) {
            FxUtils.afficherErreur("Erreur", "Impossible de retourner a la page de connexion");
        }
    }
}
