
package dal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.User;

public class UserDAO implements AutoCloseable {

    private static final String SELECT_USER =
            "SELECT u.user_id, u.role_id, r.role_name, "
            + "u.username, u.email, u.password_hash, "
            + "u.full_name, u.phone, u.status, "
            + "u.created_at, u.updated_at "
            + "FROM Users u "
            + "JOIN Roles r ON u.role_id = r.role_id ";

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();

        u.setUserId(rs.getInt("user_id"));
        u.setRoleId(rs.getInt("role_id"));
        u.setRoleName(rs.getString("role_name"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setPhone(rs.getString("phone"));
        u.setStatus(rs.getString("status"));

        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");

        if (created != null) {
            u.setCreatedAt(created.toLocalDateTime());
        }

        if (updated != null) {
            u.setUpdatedAt(updated.toLocalDateTime());
        }

        return u;
    }

    // ================= ADMIN =================

    public List<User> getAll() throws SQLException {
        List<User> list = new ArrayList<>();

        String sql = SELECT_USER + "ORDER BY u.user_id DESC";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapUser(rs));
            }
        }

        return list;
    }

    public List<User> search(String keyword) throws SQLException {
        List<User> list = new ArrayList<>();

        String sql = SELECT_USER
                + "WHERE u.username LIKE ? "
                + "OR u.email LIKE ? "
                + "OR u.full_name LIKE ? "
                + "ORDER BY u.user_id DESC";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";

            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapUser(rs));
                }
            }
        }

        return list;
    }

    public User getById(int id) throws SQLException {
        String sql = SELECT_USER + "WHERE u.user_id = ?";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        }

        return null;
    }

    public boolean updateStatus(int userId, String status)
            throws SQLException {

        if (!"ACTIVE".equals(status)
                && !"LOCKED".equals(status)) {
            throw new IllegalArgumentException("Invalid status");
        }

        String sql = "UPDATE Users SET status = ?, "
                + "updated_at = SYSDATETIME() "
                + "WHERE user_id = ?";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateRole(int userId, int roleId)
            throws SQLException {

        if (roleId != 1 && roleId != 2) {
            throw new IllegalArgumentException("Invalid role");
        }

        String sql = "UPDATE Users SET role_id = ?, "
                + "updated_at = SYSDATETIME() "
                + "WHERE user_id = ?";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, roleId);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        }
    }

    // ================= MEMBER 2 =================

    public User findById(int userId) {
        try {
            return getById(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public User findByUsername(String username) {
        String sql = SELECT_USER + "WHERE u.username = ?";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public User findByEmail(String email) {
        String sql = SELECT_USER + "WHERE u.email = ?";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean existsByUsername(String username) {
        String sql = "SELECT 1 FROM Users WHERE username = ?";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM Users WHERE email = ?";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean existsByEmailExcept(String email, int userId) {
        String sql =
                "SELECT 1 FROM Users WHERE email = ? AND user_id <> ?";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public int insertCustomer(User u) {
        String sql =
                "INSERT INTO Users "
                + "(role_id, username, email, password_hash, full_name, phone) "
                + "VALUES ("
                + "(SELECT role_id FROM Roles WHERE role_name = 'CUSTOMER'), "
                + "?, ?, ?, ?, ?)";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, u.getUsername());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPasswordHash());
            ps.setString(4, u.getFullName());
            ps.setString(5, u.getPhone());

            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public boolean updateProfile(
            int userId, String fullName, String email, String phone) {

        String sql =
                "UPDATE Users SET full_name = ?, email = ?, "
                + "phone = ?, updated_at = SYSUTCDATETIME() "
                + "WHERE user_id = ?";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setInt(4, userId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updatePassword(int userId, String newHash) {
        String sql =
                "UPDATE Users SET password_hash = ?, "
                + "updated_at = SYSUTCDATETIME() "
                + "WHERE user_id = ?";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newHash);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Tuong thich voi try-with-resources cua Member 2.
    // Moi method tu mo va dong connection.
    @Override
    public void close() {
        // Khong co connection dung chung can dong.
    }
}
