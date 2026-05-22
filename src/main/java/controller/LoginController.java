package controller;

import config.Constantes;
import config.ServiceLocator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import model.Utilisateur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AuthService;
import util.FxUtils;

import java.io.IOException;

/**
 * Controleur de la vue de connexion.
 * FIXED: utilisation du singleton ServiceLocator.getInstance() au lieu de new ServiceLocator().
 */
public class LoginController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);
    private final AuthService authService = ServiceLocator.getInstance().getAuthService();

    @FXML private TextField txtLogin;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblError;
    @FXML private ProgressIndicator progressIndicator;

    @FXML
    public void initialize() {
        lblError.setVisible(false);
        progressIndicator.setVisible(false);

        txtLogin.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) handleLogin(); });
        txtPassword.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) handleLogin(); });
    }

    @FXML
    private void handleLogin() {
        String login = txtLogin.getText().trim();
        String password = txtPassword.getText();

        if (login.isEmpty() || password.isEmpty()) {
            showError("Veuillez saisir votre login et mot de passe");
            return;
        }

        setLoading(true);

        new Thread(() -> {
            try {
                Utilisateur user = authService.login(login, password);
                javafx.application.Platform.runLater(() -> {
                    setLoading(false);
                    redirectToDashboard(user);
                });
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    setLoading(false);
                    showError(ex.getMessage());
                    LOGGER.warn("Echec connexion pour {}: {}", login, ex.getMessage());
                });
            }
        }).start();
    }

    private void redirectToDashboard(Utilisateur user) {
        try {
            String fxmlFile;
            String title;
            switch (user.getRole()) {
                case RESPONSABLE_PLANNING -> {
                    fxmlFile = "/view/ResponsableView.fxml";
                    title = "Tableau de Bord - Responsable Planning";
                }
                case RESPONSABLE_FILIERE -> {
                    fxmlFile = "/view/ResponsableFiliereView.fxml";
                    title = "Tableau de Bord - Responsable Filiere";
                }
                case ENSEIGNANT -> {
                    fxmlFile = "/view/EnseignantView.fxml";
                    title = "Tableau de Bord - Enseignant";
                }
                default -> throw new IllegalStateException("Role non supporte");
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Object ctrl = loader.getController();
            if (ctrl instanceof DashboardController dc) {
                dc.setUtilisateur(user);
            }

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setTitle(title + " | " + Constantes.APP_NAME);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

            LOGGER.info("Redirection vers {} pour {}", title, user.getLogin());

        } catch (IOException ex) {
            LOGGER.error("Erreur chargement dashboard", ex);
            FxUtils.afficherErreur("Erreur", "Impossible de charger le tableau de bord");
        }
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }

    private void setLoading(boolean loading) {
        btnLogin.setDisable(loading);
        progressIndicator.setVisible(loading);
        txtLogin.setDisable(loading);
        txtPassword.setDisable(loading);
    }
}
