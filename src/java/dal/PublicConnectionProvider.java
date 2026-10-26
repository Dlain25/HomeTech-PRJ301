package dal;

import java.sql.Connection;
import java.sql.SQLException;

/** Integration seam: every call must return a new connection owned by the caller. */
@FunctionalInterface
public interface PublicConnectionProvider {
    Connection open() throws SQLException;
}

