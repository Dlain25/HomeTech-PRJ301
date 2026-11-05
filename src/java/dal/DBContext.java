package dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Shared SQL Server connection factory for public, customer and admin modules. */
public final class DBContext {
    private DBContext() { }
    public static Connection getConnection() throws SQLException {
        Properties local = localSettings();
        String url = setting("hometech.db.url", "HOMETECH_DB_URL", local);
        if (url == null || !url.startsWith("jdbc:sqlserver:")) {
            throw new SQLException("Public database URL is missing or invalid. Configure HOMETECH_DB_URL or ~/.hometech/public-db.properties.", "HT001");
        }
        Properties properties = new Properties();
        String user = setting("hometech.db.user", "HOMETECH_DB_USER", local);
        String password = setting("hometech.db.password", "HOMETECH_DB_PASSWORD", local);
        if (user != null) properties.setProperty("user", user);
        if (password != null) properties.setProperty("password", password);
        properties.setProperty("loginTimeout", "5");
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("SQL Server JDBC driver is unavailable.", "HT002");
        }
        return DriverManager.getConnection(url, properties);
    }
    private static String setting(String property, String environment, Properties local) {
        String value = System.getProperty(property);
        if (value == null) value = System.getenv(environment);
        return value == null ? local.getProperty(property) : value;
    }
    private static Properties localSettings() throws SQLException {
        Properties local = new Properties();
        String configured = System.getProperty("hometech.db.config");
        if (configured == null) configured = System.getenv("HOMETECH_DB_CONFIG");
        try {
            Path path = configured == null
                    ? Path.of(System.getProperty("user.home"), ".hometech", "public-db.properties")
                    : Path.of(configured);
            // Some Windows launchers report the drive root as user.home.
            String profile = System.getenv("USERPROFILE");
            if (configured == null && !Files.exists(path) && profile != null) {
                path = Path.of(profile, ".hometech", "public-db.properties");
            }
            if (configured == null && !Files.exists(path)) return local;
            try (java.io.BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                reader.mark(1);
                if (reader.read() != '\uFEFF') reader.reset();
                local.load(reader);
            }
            return local;
        } catch (IOException | IllegalArgumentException ex) {
            // Never include file contents, credentials or a JDBC URL in an exception.
            throw new SQLException("Cannot read public database local configuration.", "HT003");
        }
    }
}
