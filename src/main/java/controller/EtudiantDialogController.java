package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Etudiant;
import model.Promotion;

import java.time.LocalDate;
import java.util.List;

public class EtudiantDialogController {

    @FXML private TextField txtCne, txtNom, txtPrenom, txtEmail, txtTelephone;
    @FXML private DatePicker dpDateNaissance;
    @FXML private ComboBox<Promotion> cbPromotion;
    @FXML private Button btnSave, btnCancel;

    private Etudiant etudiant;
    private boolean saved = false;

    @FXML
    public void initialize() {
        btnSave.setOnAction(e -> handleSave());
        btnCancel.setOnAction(e -> { saved = false; btnCancel.getScene().getWindow().hide(); });
    }

    public void setEtudiant(Etudiant e) {
        this.etudiant = e != null ? e : new Etudiant();
        if (e != null) {
            txtCne.setText(e.getCne());
            txtNom.setText(e.getNom());
            txtPrenom.setText(e.getPrenom());
            txtEmail.setText(e.getEmail());
            txtTelephone.setText(e.getTelephone());
            dpDateNaissance.setValue(e.getDateNaissance());
        }
    }

    public void setPromotions(List<Promotion> promotions) {
        cbPromotion.setItems(javafx.collections.FXCollections.observableArrayList(promotions));
    }

    private void handleSave() {
        etudiant.setCne(txtCne.getText().trim());
        etudiant.setNom(txtNom.getText().trim());
        etudiant.setPrenom(txtPrenom.getText().trim());
        etudiant.setEmail(txtEmail.getText().trim());
        etudiant.setTelephone(txtTelephone.getText().trim());
        etudiant.setDateNaissance(dpDateNaissance.getValue());
        saved = true;
        btnSave.getScene().getWindow().hide();
    }

    public Etudiant getEtudiant() { return etudiant; }
    public Promotion getSelectedPromotion() { return cbPromotion.getValue(); }
    public boolean isSaved() { return saved; }
}
