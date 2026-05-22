package ma.ecole;

import config.AppConfig;
import config.Constantes;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class MainApp extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainApp.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            LOGGER.info("Demarrage de {} v{}", AppConfig.get().getAppName(), Constantes.APP_VERSION);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());

            primaryStage.setTitle(AppConfig.get().getAppName() + " v" + Constantes.APP_VERSION);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(Constantes.APP_MIN_WIDTH);
            primaryStage.setMinHeight(Constantes.APP_MIN_HEIGHT);
            primaryStage.centerOnScreen();

            // Icon
            try {
                primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon.png"))));
            } catch (Exception e) {
                LOGGER.warn("Icone non trouvee");
            }

            primaryStage.show();
            LOGGER.info("Application demarree avec succes");

        } catch (IOException ex) {
            LOGGER.error("Erreur demarrage application", ex);
            System.err.println("Impossible de demarrer l'application: " + ex.getMessage());
        }
    }

    @Override
    public void stop() {
        LOGGER.info("Arret de l'application");
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
