package dal;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBContext {

    private DBContext() {
    }

    public static Connection getConnection() throws SQLException {
        Properties local = localSettings();

        String url = setting(
                "hometech.db.url",
                "HOMETECH_DB_URL",
                local
        );

        if (url == null || !url.startsWith("jdbc:sqlserver:")) {
            throw new SQLException(
                    "Chưa cấu hình URL SQL Server. Kiểm tra "
                    + "HOMETECH_DB_URL hoặc file public-db.properties.",
                    "HT001"
            );
        }

        String user = setting(
                "hometech.db.user",
                "HOMETECH_DB_USER",
                local
        );

        String password = setting(
                "hometech.db.password",
                "HOMETECH_DB_PASSWORD",
                local
        );

        Properties properties = new Properties();

        if (user != null) {
            properties.setProperty("user", user);
        }

        if (password != null) {
            properties.setProperty("password", password);
        }

        properties.setProperty("loginTimeout", "5");

        try {
            Class.forName(
                    "com.microsoft.sqlserver.jdbc.SQLServerDriver"
            );
        } catch (ClassNotFoundException ex) {
            throw new SQLException(
                    "Không tìm thấy SQL Server JDBC Driver.",
                    "HT002"
            );
        }

        return DriverManager.getConnection(url, properties);
    }

    private static String setting(
            String property,
            String environment,
            Properties local) {

        // Ưu tiên: JVM property → biến môi trường → file cấu hình.
        String value = System.getProperty(property);

        if (value == null) {
            value = System.getenv(environment);
        }

        if (value == null) {
            value = local.getProperty(property);
        }

        return value;
    }

    private static Properties localSettings() throws SQLException {
        Properties local = new Properties();

        String configured = System.getProperty("hometech.db.config");

        if (configured == null) {
            configured = System.getenv("HOMETECH_DB_CONFIG");
        }

        try {
            Path path = configured == null
                    ? Path.of(
                            System.getProperty("user.home"),
                            ".hometech",
                            "public-db.properties"
                    )
                    : Path.of(configured);

            String profile = System.getenv("USERPROFILE");

            if (configured == null
                    && !Files.exists(path)
                    && profile != null) {

                path = Path.of(
                        profile,
                        ".hometech",
                        "public-db.properties"
                );
            }

            if (configured == null && !Files.exists(path)) {
                return local;
            }

            try (BufferedReader reader = Files.newBufferedReader(
                    path, StandardCharsets.UTF_8)) {

                // Bỏ ký tự BOM nếu file được lưu dưới dạng UTF-8 BOM.
                reader.mark(1);

                if (reader.read() != '\uFEFF') {
                    reader.reset();
                }

                local.load(reader);
            }

            return local;

        } catch (IOException | IllegalArgumentException ex) {
            throw new SQLException(
                    "Không đọc được file cấu hình database.",
                    "HT003"
            );
        }
    }
}