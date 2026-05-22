package config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Charge et expose config.properties depuis le classpath.
 * Singleton thread-safe via initialisation statique.
 */
public final class AppConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);
    private static final String FICHIER_CONFIG = "/config.properties";
    private static final AppConfig INSTANCE = new AppConfig();

    private final Properties props = new Properties();

    private AppConfig() {
        chargerConfiguration();
        creerDossiersExport();
    }

    public static AppConfig get() { return INSTANCE; }

    private void chargerConfiguration() {
        try (InputStream in = AppConfig.class.getResourceAsStream(FICHIER_CONFIG)) {
            if (in == null) {
                LOGGER.warn("Fichier config.properties introuvable dans le classpath, utilisation des valeurs par defaut");
                definirValeursParDefaut();
                return;
            }
            props.load(in);
            LOGGER.info("Configuration chargee avec succes");
        } catch (IOException ex) {
            LOGGER.error("Erreur lecture config.properties", ex);
            definirValeursParDefaut();
        }
    }

    private void definirValeursParDefaut() {
        props.setProperty("db.url", "jdbc:mysql://localhost:3306/gestion_ecole?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Africa/Casablanca");
        props.setProperty("db.user", "root");
        props.setProperty("db.password", "root");
        props.setProperty("db.pool.max", "10");
        props.setProperty("db.pool.min", "2");
        props.setProperty("export.path", "exports/");
        props.setProperty("app.name", "Gestion des Notes");
        props.setProperty("app.version", "2.0.0");
        props.setProperty("app.page.size", "50");
    }

    // ── Base de donnees ───────────────────────────────────────────────────────
    public String getDatabaseUrl() { return props.getProperty("db.url"); }
    public String getDatabaseUser() { return props.getProperty("db.user"); }
    public String getDatabasePassword() { return props.getProperty("db.password", ""); }
    public int getPoolMax() { return intProp("db.pool.max", 10); }
    public int getPoolMin() { return intProp("db.pool.min", 2); }

    // ── Exports ───────────────────────────────────────────────────────────────
    public String getExportPath() { return props.getProperty("export.path", "exports/"); }
    public String getLogoPath() { return props.getProperty("export.pdf.logo", ""); }

    // ── Application ───────────────────────────────────────────────────────────
    public String getAppName() { return props.getProperty("app.name", "Gestion Notes"); }
    public String getAppVersion() { return props.getProperty("app.version", "2.0.0"); }
    public int getPageSize() { return intProp("app.page.size", Constantes.MAX_ETUDIANTS_PAR_PAGE); }

    // ── Acces generique ───────────────────────────────────────────────────────
    public String get(String cle) { return props.getProperty(cle); }
    public String get(String cle, String defaut) { return props.getProperty(cle, defaut); }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private int intProp(String cle, int defaut) {
        try { return Integer.parseInt(props.getProperty(cle)); }
        catch (Exception ex) { return defaut; }
    }

    private void creerDossiersExport() {
        Path exportDir = Paths.get(getExportPath());
        if (!Files.exists(exportDir)) {
            try { 
                Files.createDirectories(exportDir);
                LOGGER.info("Dossier d'export cree: {}", exportDir.toAbsolutePath());
            }
            catch (IOException ex) {
                LOGGER.warn("Impossible de creer le dossier export: {}", ex.getMessage());
            }
        }
    }
}
