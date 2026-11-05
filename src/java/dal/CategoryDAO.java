
package dal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Category;

public class CategoryDAO {
    private final PublicConnectionProvider connections;
    public CategoryDAO() { this(DBContext::getConnection); }
    public CategoryDAO(PublicConnectionProvider connections) { this.connections = connections; }


    // ADMIN: Lay tat ca danh muc
    public List<Category> getAll() throws SQLException {
        List<Category> list = new ArrayList<>();

        String sql = "SELECT category_id, category_name, description, is_active "
                   + "FROM Categories ORDER BY category_id DESC";

        try (Connection con = connections.open();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Category(
                    rs.getInt("category_id"),
                    rs.getString("category_name"),
                    rs.getString("description"),
                    rs.getBoolean("is_active")
                ));
            }
        }
        return list;
    }

    // FRONTEND: Lay danh muc ACTIVE
    public List<Category> findActive() throws SQLException {
        List<Category> list = new ArrayList<>();

        String sql = "SELECT category_id, category_name, description, is_active "
                   + "FROM Categories "
                   + "WHERE is_active = 1 "
                   + "ORDER BY category_name";

        try (Connection con = connections.open();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setQueryTimeout(10);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Category(
                        rs.getInt("category_id"),
                        rs.getString("category_name"),
                        rs.getString("description"),
                        rs.getBoolean("is_active")
                    ));
                }
            }
        }

        return list;
    }

    // ADMIN: Tim danh muc theo ID
    public Category getById(int id) throws SQLException {
        String sql = "SELECT category_id, category_name, description, is_active "
                   + "FROM Categories WHERE category_id = ?";

        try (Connection con = connections.open();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Category(
                        rs.getInt("category_id"),
                        rs.getString("category_name"),
                        rs.getString("description"),
                        rs.getBoolean("is_active")
                    );
                }
            }
        }
        return null;
    }

    // ADMIN: Them danh muc
    public void insert(Category c) throws SQLException {
        String sql = "INSERT INTO Categories "
                   + "(category_name, description, is_active) VALUES (?, ?, ?)";

        try (Connection con = connections.open();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getCategoryName());
            ps.setString(2, c.getDescription());
            ps.setBoolean(3, c.isActive());
            ps.executeUpdate();
        }
    }

    // ADMIN: Cap nhat danh muc
    public void update(Category c) throws SQLException {
        String sql = "UPDATE Categories SET category_name = ?, "
                   + "description = ?, is_active = ? WHERE category_id = ?";

        try (Connection con = connections.open();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getCategoryName());
            ps.setString(2, c.getDescription());
            ps.setBoolean(3, c.isActive());
            ps.setInt(4, c.getCategoryId());
            ps.executeUpdate();
        }
    }

    // ADMIN: Xoa danh muc
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM Categories WHERE category_id = ?";

        try (Connection con = connections.open();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
