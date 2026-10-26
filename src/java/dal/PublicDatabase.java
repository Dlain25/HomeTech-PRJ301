package dal;

import java.sql.Connection;
import java.sql.SQLException;

public final class PublicDatabase {

    private PublicDatabase() {
    }

    public static Connection open() throws SQLException {
        return DBContext.getConnection();
    }
}