package config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static final HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();
            
            // Load from config.properties via AppConfig
            String jdbcUrl = AppConfig.get("db.url");
            String username = AppConfig.get("db.user");
            String password = AppConfig.get("db.password");
            String driver = AppConfig.get("db.driver");
            
            // Validate required values
            if (jdbcUrl == null || jdbcUrl.trim().isEmpty()) {
                throw new IllegalStateException("db.url is missing in config.properties");
            }
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalStateException("db.user is missing in config.properties");
            }
            
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password != null ? password : "");
            
            if (driver != null && !driver.trim().isEmpty()) {
                config.setDriverClassName(driver);
            }
            
            // Pool settings
            String maxPool = AppConfig.get("db.pool.maxSize");
            String minIdle = AppConfig.get("db.pool.minIdle");
            String timeout = AppConfig.get("db.pool.timeout");
            
            config.setMaximumPoolSize(maxPool != null ? Integer.parseInt(maxPool) : 10);
            config.setMinimumIdle(minIdle != null ? Integer.parseInt(minIdle) : 2);
            config.setConnectionTimeout(timeout != null ? Long.parseLong(timeout) : 30000);
            
            // MySQL optimizations
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            
            dataSource = new HikariDataSource(config);
            logger.info("Database pool initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize database pool", e);
            throw new ExceptionInInitializerError("Impossible d'initialiser la connexion BD: " + e.getMessage());
        }
    }

    private DatabaseConnection() {
        // Singleton
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database pool closed");
        }
    }
}