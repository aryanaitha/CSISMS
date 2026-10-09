package com.csims.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton database connection manager for JDBC connections.
 * Reads db.properties from the classpath or filesystem.
 */
public final class DBConnection {

    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());
    private static volatile DBConnection instance;

    private String url;
    private String user;
    private String password;
    private String driver;
    private boolean initialized = false;

    private DBConnection() {
        loadProperties();
    }

    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) {
                    instance = new DBConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Reload configuration properties.
     */
    public synchronized void loadProperties() {
        Properties props = new Properties();
        java.io.File externalFile = new java.io.File("db.properties");
        if (!externalFile.exists()) {
            externalFile = new java.io.File("src/main/resources/db.properties");
        }

        if (externalFile.exists()) {
            try (java.io.FileInputStream fis = new java.io.FileInputStream(externalFile)) {
                props.load(fis);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error reading external db.properties: {0}", e.getMessage());
            }
        }

        if (props.isEmpty()) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
                if (in != null) {
                    props.load(in);
                } else {
                    LOGGER.warning("db.properties not found on classpath, trying defaults");
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error loading db.properties: {0}", e.getMessage());
            }
        }

        this.url = props.getProperty("db.url", props.getProperty("url", "jdbc:mysql://localhost:3306/csims_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"));
        this.user = props.getProperty("db.user", props.getProperty("user", "root"));
        this.password = props.getProperty("db.password", props.getProperty("password", "root"));
        this.driver = props.getProperty("db.driver", props.getProperty("driver", "com.mysql.cj.jdbc.Driver"));

        try {
            Class.forName(this.driver);
            this.initialized = true;
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "JDBC Driver class not found: " + this.driver, e);
            this.initialized = false;
        }
    }

    /**
     * Allows custom override for test suite or alternate settings.
     */
    public synchronized void setCustomConfig(String url, String user, String password, String driver) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.driver = driver;
        try {
            Class.forName(this.driver);
            this.initialized = true;
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Failed loading driver " + driver, e);
        }
    }

    /**
     * Opens and returns a new JDBC Connection.
     * Callers must close connection using try-with-resources.
     */
    public Connection getConnection() throws SQLException {
        if (!initialized) {
            loadProperties();
        }
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Checks if the database is reachable with a short timeout.
     */
    public boolean isConnectionAvailable() {
        try {
            DriverManager.setLoginTimeout(3);
            try (Connection conn = getConnection()) {
                return conn != null && !conn.isClosed() && conn.isValid(2);
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Database connection check failed: {0}", e.getMessage());
            return false;
        }
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }
}
