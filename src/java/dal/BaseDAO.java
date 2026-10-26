package dal;

import java.sql.Connection;
import java.sql.SQLException;

public class BaseDAO implements AutoCloseable {

    protected final Connection connection;

    public BaseDAO() throws SQLException {
        connection = DBContext.getConnection();
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}