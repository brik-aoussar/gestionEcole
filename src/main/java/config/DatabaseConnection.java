package config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Singleton pool de connexions HikariCP.
 * FIXED: coherence des cles avec AppConfig (db.pool.maxSize/minIdle/timeout), driverClassName optionnel,
 *        shutdown propre, gestion des valeurs manquantes.
 */
public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static volatile HikariDataSource dataSource;
    private static final Object LOCK = new Object();

    static {
        initDataSource();
    }

    private static void initDataSource() {
        try {
            HikariConfig config = new HikariConfig();

            String jdbcUrl = AppConfig.get().getDatabaseUrl();
            String username = AppConfig.get().getDatabaseUser();
            String password = AppConfig.get().getDatabasePassword();
            String driver = AppConfig.get().getDatabaseDriver();

            if (jdbcUrl == null || jdbcUrl.trim().isEmpty()) {
                throw new IllegalStateException("db.url est manquant dans config.properties");
            }
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalStateException("db.user est manquant dans config.properties");
            }

            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password != null ? password : "");

            if (driver != null && !driver.trim().isEmpty()) {
                config.setDriverClassName(driver);
            }

            // FIXED: coherence des cles avec AppConfig
            config.setMaximumPoolSize(AppConfig.get().getPoolMax());
            config.setMinimumIdle(AppConfig.get().getPoolMin());
            config.setConnectionTimeout(AppConfig.get().getPoolTimeout());

            // MySQL optimizations
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");

            synchronized (LOCK) {
                if (dataSource == null || dataSource.isClosed()) {
                    dataSource = new HikariDataSource(config);
                }
            }
            logger.info("Database pool initialized: max={}, minIdle={}, timeout={}ms",
                    config.getMaximumPoolSize(), config.getMinimumIdle(), config.getConnectionTimeout());

        } catch (Exception e) {
            logger.error("Failed to initialize database pool", e);
            throw new ExceptionInInitializerError("Impossible d'initialiser la connexion BD: " + e.getMessage());
        }
    }

    private DatabaseConnection() {
        // Singleton
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            synchronized (LOCK) {
                if (dataSource == null || dataSource.isClosed()) {
                    initDataSource();
                }
            }
        }
        return dataSource.getConnection();
    }

    public static void close() {
        synchronized (LOCK) {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
                logger.info("Database pool closed");
            }
        }
    }

    public static boolean isHealthy() {
        try (Connection c = getConnection()) {
            return c != null && c.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
}
