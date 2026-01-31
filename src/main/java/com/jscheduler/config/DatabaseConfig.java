package com.jscheduler.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database configuration singleton managing HikariCP connection pool.
 * Loads configuration from database.properties and provides database connections.
 */
public class DatabaseConfig {
    private static DatabaseConfig instance;
    private final HikariDataSource dataSource;

    private DatabaseConfig() {
        Properties props = loadProperties();
        HikariConfig config = new HikariConfig();

        // Connection settings
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));

        // Pool settings
        config.setMaximumPoolSize(Integer.parseInt(
            props.getProperty("db.pool.maximumPoolSize", "10")));
        config.setMinimumIdle(Integer.parseInt(
            props.getProperty("db.pool.minimumIdle", "2")));
        config.setConnectionTimeout(Long.parseLong(
            props.getProperty("db.pool.connectionTimeout", "30000")));
        config.setIdleTimeout(Long.parseLong(
            props.getProperty("db.pool.idleTimeout", "600000")));
        config.setMaxLifetime(Long.parseLong(
            props.getProperty("db.pool.maxLifetime", "1800000")));

        // Additional settings
        config.setAutoCommit(false); // Explicit transaction control
        config.setConnectionTestQuery("SELECT 1");

        this.dataSource = new HikariDataSource(config);
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("database.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find database.properties");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load database configuration", e);
        }
        return props;
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
