
package dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBContext {

    private static final String URL =
            "jdbc:sqlserver://DESKTOP-UDDE295:1433;"
            + "databaseName=HomeTechDB;"
            + "encrypt=true;"
            + "trustServerCertificate=true;";

    private static final String USER = "sa";

    // Thay bằng mật khẩu SQL Server của bạn
    private static final String PASSWORD = System.getenv("HOMETECH_DB_PASSWORD");

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("SQL Server JDBC Driver loaded successfully!");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                    "Cannot load SQL Server JDBC Driver: " + e.getMessage()
            );
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
