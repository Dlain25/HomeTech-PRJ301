package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.CartItem;

public class CartDAO extends BaseDAO {
public CartDAO() throws SQLException {
    super();
}
    private Integer findCartId(int userId) throws SQLException {
        String sql = "SELECT cart_id FROM Carts WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return null;
            }
        }
    }

    private int getOrCreateCartId(int userId) throws SQLException {
        Integer id = findCartId(userId);
        if (id != null) {
            return id;
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO Carts (user_id) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            Integer again = findCartId(userId);
            if (again != null) {
                return again;
            }
            throw e;
        }
        throw new SQLException("Không tạo được giỏ hàng");
    }

    private void touchCart(int cartId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE Carts SET updated_at = SYSUTCDATETIME() WHERE cart_id = ?")) {
            ps.setInt(1, cartId);
            ps.executeUpdate();
        }
    }

    public String addItem(int userId, int variantId, int quantity) {
        if (quantity < 1) {
            return "Số lượng không hợp lệ";
        }
        String sqlVariant = "SELECT v.is_active, p.status, COALESCE(i.quantity_available, 0) AS stock "
                + "FROM ProductVariants v JOIN Products p ON p.product_id = v.product_id "
                + "LEFT JOIN Inventory i ON i.variant_id = v.variant_id WHERE v.variant_id = ?";
        try {
            int stock = 0;
            try (PreparedStatement ps = connection.prepareStatement(sqlVariant)) {
                ps.setInt(1, variantId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return "Sản phẩm không tồn tại";
                    }
                    if (!rs.getBoolean("is_active") || !"ACTIVE".equals(rs.getString("status"))) {
                        return "Sản phẩm hiện không được bán";
                    }
                    stock = rs.getInt("stock");
                }
            }
            int cartId = getOrCreateCartId(userId);
            int current = 0;
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT quantity FROM CartItems WHERE cart_id = ? AND variant_id = ?")) {
                ps.setInt(1, cartId);
                ps.setInt(2, variantId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        current = rs.getInt(1);
                    }
                }
            }
            if (current + quantity > stock) {
                if (stock == 0) {
                    return "Sản phẩm đã hết hàng";
                }
                return "Chỉ còn " + stock + " sản phẩm trong kho (trong giỏ bạn đã có " + current + ")";
            }
            if (current > 0) {
                try (PreparedStatement ps = connection.prepareStatement(
                        "UPDATE CartItems SET quantity = quantity + ? WHERE cart_id = ? AND variant_id = ?")) {
                    ps.setInt(1, quantity);
                    ps.setInt(2, cartId);
                    ps.setInt(3, variantId);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO CartItems (cart_id, variant_id, quantity) VALUES (?, ?, ?)")) {
                    ps.setInt(1, cartId);
                    ps.setInt(2, variantId);
                    ps.setInt(3, quantity);
                    ps.executeUpdate();
                }
            }
            touchCart(cartId);
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return "Không thể thêm vào giỏ hàng lúc này, vui lòng thử lại";
        }
    }

    public String updateQuantity(int userId, int cartItemId, int quantity) {
        if (quantity < 1) {
            return removeItem(userId, cartItemId) ? null : "Không tìm thấy sản phẩm trong giỏ";
        }
        String sql = "SELECT ci.cart_id, COALESCE(i.quantity_available, 0) AS stock "
                + "FROM CartItems ci JOIN Carts c ON c.cart_id = ci.cart_id "
                + "LEFT JOIN Inventory i ON i.variant_id = ci.variant_id "
                + "WHERE ci.cart_item_id = ? AND c.user_id = ?";
        try {
            int cartId;
            int stock;
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, cartItemId);
                ps.setInt(2, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return "Không tìm thấy sản phẩm trong giỏ";
                    }
                    cartId = rs.getInt("cart_id");
                    stock = rs.getInt("stock");
                }
            }
            if (quantity > stock) {
                return "Chỉ còn " + stock + " sản phẩm trong kho";
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE CartItems SET quantity = ? WHERE cart_item_id = ?")) {
                ps.setInt(1, quantity);
                ps.setInt(2, cartItemId);
                ps.executeUpdate();
            }
            touchCart(cartId);
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return "Không thể cập nhật giỏ hàng lúc này";
        }
    }

    public boolean removeItem(int userId, int cartItemId) {
        String sql = "DELETE ci FROM CartItems ci JOIN Carts c ON c.cart_id = ci.cart_id "
                + "WHERE ci.cart_item_id = ? AND c.user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, cartItemId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<CartItem> getItems(int userId) {
        List<CartItem> list = new ArrayList<>();
        String sql = "SELECT ci.cart_item_id, ci.variant_id, p.product_id, p.product_name, "
                + "v.variant_name, v.color, v.price, ci.quantity, "
                + "COALESCE(i.quantity_available, 0) AS stock, "
                + "(SELECT TOP 1 pi.image_url FROM ProductImages pi WHERE pi.product_id = p.product_id "
                + " ORDER BY pi.is_primary DESC, pi.sort_order, pi.image_id) AS image_url "
                + "FROM Carts c JOIN CartItems ci ON ci.cart_id = c.cart_id "
                + "JOIN ProductVariants v ON v.variant_id = ci.variant_id "
                + "JOIN Products p ON p.product_id = v.product_id "
                + "LEFT JOIN Inventory i ON i.variant_id = v.variant_id "
                + "WHERE c.user_id = ? ORDER BY ci.added_at DESC, ci.cart_item_id DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItem it = new CartItem();
                    it.setCartItemId(rs.getInt("cart_item_id"));
                    it.setVariantId(rs.getInt("variant_id"));
                    it.setProductId(rs.getInt("product_id"));
                    it.setProductName(rs.getString("product_name"));
                    it.setVariantName(rs.getString("variant_name"));
                    it.setColor(rs.getString("color"));
                    it.setUnitPrice(rs.getBigDecimal("price"));
                    it.setQuantity(rs.getInt("quantity"));
                    it.setStock(rs.getInt("stock"));
                    it.setImageUrl(rs.getString("image_url"));
                    list.add(it);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
