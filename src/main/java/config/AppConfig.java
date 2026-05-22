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
 * FIXED: coherence des cles, creation des sous-dossiers pdf/excel, gestion des valeurs par defaut correctes.
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
        props.setProperty("db.url", "jdbc:mysql://localhost:3306/gestion_ecole?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Africa/Casablanca&useSSL=false&allowPublicKeyRetrieval=true");
        props.setProperty("db.user", "root");
        props.setProperty("db.password", "root");
        props.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        props.setProperty("db.pool.maxSize", "10");
        props.setProperty("db.pool.minIdle", "2");
        props.setProperty("db.pool.timeout", "30000");
        props.setProperty("export.path", "exports/");
        props.setProperty("export.pdf.path", "exports/pdf/");
        props.setProperty("export.excel.path", "exports/excel/");
        props.setProperty("app.name", "Gestion des Notes");
        props.setProperty("app.version", "2.0.0");
        props.setProperty("app.page.size", "50");
    }

    // ── Base de donnees ───────────────────────────────────────────────────────
    public String getDatabaseUrl() { return props.getProperty("db.url"); }
    public String getDatabaseUser() { return props.getProperty("db.user"); }
    public String getDatabasePassword() { return props.getProperty("db.password", ""); }
    public String getDatabaseDriver() { return props.getProperty("db.driver"); }
    public int getPoolMax() { return intProp("db.pool.maxSize", 10); }
    public int getPoolMin() { return intProp("db.pool.minIdle", 2); }
    public long getPoolTimeout() { return longProp("db.pool.timeout", 30000L); }

    // ── Exports ───────────────────────────────────────────────────────────────
    public String getExportPath() { return props.getProperty("export.path", "exports/"); }
    public String getPdfExportPath() { return props.getProperty("export.pdf.path", "exports/pdf/"); }
    public String getExcelExportPath() { return props.getProperty("export.excel.path", "exports/excel/"); }
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
    private long longProp(String cle, long defaut) {
        try { return Long.parseLong(props.getProperty(cle)); }
        catch (Exception ex) { return defaut; }
    }

    private void creerDossiersExport() {
        String[] paths = { getExportPath(), getPdfExportPath(), getExcelExportPath() };
        for (String p : paths) {
            if (p == null || p.isBlank()) continue;
            Path dir = Paths.get(p);
            if (!Files.exists(dir)) {
                try {
                    Files.createDirectories(dir);
                    LOGGER.info("Dossier d'export cree: {}", dir.toAbsolutePath());
                } catch (IOException ex) {
                    LOGGER.warn("Impossible de creer le dossier export {}: {}", dir, ex.getMessage());
                }
            }
        }
    }
}
