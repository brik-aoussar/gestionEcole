package util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public final class FxUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FxUtils.class);

    private FxUtils() {}

    public static void afficherErreur(String titre, String message) {
        LOGGER.error("{}: {}", titre, message);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(titre);
        alert.setContentText(message);
        styliserAlert(alert);
        alert.showAndWait();
    }

    public static void afficherInfo(String titre, String message) {
        LOGGER.info("{}: {}", titre, message);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(titre);
        alert.setContentText(message);
        styliserAlert(alert);
        alert.showAndWait();
    }

    public static void afficherAvertissement(String titre, String message) {
        LOGGER.warn("{}: {}", titre, message);
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attention");
        alert.setHeaderText(titre);
        alert.setContentText(message);
        styliserAlert(alert);
        alert.showAndWait();
    }

    public static boolean confirmer(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(titre);
        alert.setContentText(message);
        styliserAlert(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static Optional<String> demanderSaisie(String titre, String message, String defaut) {
        TextInputDialog dialog = new TextInputDialog(defaut);
        dialog.setTitle(titre);
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        return dialog.showAndWait();
    }

    public static void styliserAlert(Alert alert) {
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().clear();
        alert.getDialogPane().setStyle(
            "-fx-font-family: 'Segoe UI', sans-serif;" +
            "-fx-font-size: 13px;"
        );
    }

    public static String formatNote(double note) {
        return String.format("%.2f", note);
    }
}
