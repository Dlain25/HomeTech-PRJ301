
package dal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.User;

public class UserDAO {

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();

        u.setUserId(rs.getInt("user_id"));
        u.setRoleId(rs.getInt("role_id"));
        u.setRoleName(rs.getString("role_name"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
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

    public List<User> getAll() throws SQLException {
        List<User> list = new ArrayList<>();

        String sql = "SELECT u.user_id, u.role_id, r.role_name, "
                + "u.username, u.email, u.full_name, u.phone, "
                + "u.status, u.created_at, u.updated_at "
                + "FROM Users u "
                + "JOIN Roles r ON u.role_id = r.role_id "
                + "ORDER BY u.user_id DESC";

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

        String sql = "SELECT u.user_id, u.role_id, r.role_name, "
                + "u.username, u.email, u.full_name, u.phone, "
                + "u.status, u.created_at, u.updated_at "
                + "FROM Users u "
                + "JOIN Roles r ON u.role_id = r.role_id "
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
        String sql = "SELECT u.user_id, u.role_id, r.role_name, "
                + "u.username, u.email, u.full_name, u.phone, "
                + "u.status, u.created_at, u.updated_at "
                + "FROM Users u "
                + "JOIN Roles r ON u.role_id = r.role_id "
                + "WHERE u.user_id = ?";

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
}
