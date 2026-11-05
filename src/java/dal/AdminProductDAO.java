
package dal;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdminProductDAO extends BaseDAO {

    public AdminProductDAO() throws SQLException {
        super();
    }

    // DANH SACH SAN PHAM, PHIEN BAN VA TON KHO
    public List<Map<String, Object>> getAll() throws SQLException {
        String sql =
            "SELECT p.product_id, p.product_name, p.sku, " +
            "p.description, p.category_id, p.brand_id, " +
            "p.base_price, p.warranty_months, p.is_featured, " +
            "p.status, c.category_name, b.brand_name, " +
            "v.variant_id, v.variant_sku, v.variant_name, " +
            "v.color, v.price, v.is_active, " +
            "COALESCE(i.quantity_available, 0) AS stock, " +
            "img.image_url " +
            "FROM Products p " +
            "JOIN Categories c ON p.category_id = c.category_id " +
            "JOIN Brands b ON p.brand_id = b.brand_id " +
            "LEFT JOIN ProductVariants v ON p.product_id = v.product_id " +
            "LEFT JOIN Inventory i ON v.variant_id = i.variant_id " +
            "OUTER APPLY ( " +
            " SELECT TOP 1 pi.image_url " +
            " FROM ProductImages pi " +
            " WHERE pi.product_id = p.product_id " +
            " ORDER BY pi.is_primary DESC, pi.sort_order, pi.image_id " +
            ") img " +
            "ORDER BY p.product_id DESC, v.variant_id";

        List<Map<String, Object>> result = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapProduct(rs));
            }
        }

        return result;
    }

    // LAY THONG TIN MOT SAN PHAM
    public Map<String, Object> getById(int productId)
            throws SQLException {

        String sql =
            "SELECT product_id, category_id, brand_id, sku, " +
            "product_name, description, base_price, " +
            "warranty_months, is_featured, status " +
            "FROM Products WHERE product_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> p = new LinkedHashMap<>();
                    p.put("productId", rs.getInt("product_id"));
                    p.put("categoryId", rs.getInt("category_id"));
                    p.put("brandId", rs.getInt("brand_id"));
                    p.put("sku", rs.getString("sku"));
                    p.put("productName", rs.getString("product_name"));
                    p.put("description", rs.getString("description"));
                    p.put("basePrice", rs.getBigDecimal("base_price"));
                    p.put("warrantyMonths", rs.getInt("warranty_months"));
                    p.put("featured", rs.getBoolean("is_featured"));
                    p.put("status", rs.getString("status"));
                    return p;
                }
            }
        }

        return null;
    }

    private Map<String, Object> mapProduct(ResultSet rs)
            throws SQLException {

        Map<String, Object> p = new LinkedHashMap<>();

        p.put("productId", rs.getInt("product_id"));
        p.put("productName", rs.getString("product_name"));
        p.put("sku", rs.getString("sku"));
        p.put("description", rs.getString("description"));
        p.put("categoryId", rs.getInt("category_id"));
        p.put("brandId", rs.getInt("brand_id"));
        p.put("categoryName", rs.getString("category_name"));
        p.put("brandName", rs.getString("brand_name"));
        p.put("basePrice", rs.getBigDecimal("base_price"));
        p.put("warrantyMonths", rs.getInt("warranty_months"));
        p.put("featured", rs.getBoolean("is_featured"));
        p.put("status", rs.getString("status"));

        int variantId = rs.getInt("variant_id");
        p.put("variantId", rs.wasNull() ? null : variantId);

        p.put("variantSku", rs.getString("variant_sku"));
        p.put("variantName", rs.getString("variant_name"));
        p.put("color", rs.getString("color"));
        p.put("price", rs.getBigDecimal("price"));
        p.put("variantActive", rs.getObject("is_active"));
        p.put("stock", rs.getInt("stock"));
        p.put("imageUrl", rs.getString("image_url"));

        return p;
    }

    // DANH MUC DANG HOAT DONG
    public List<Map<String, Object>> getCategories()
            throws SQLException {

        String sql =
            "SELECT category_id, category_name " +
            "FROM Categories WHERE is_active = 1 " +
            "ORDER BY category_name";

        List<Map<String, Object>> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> c = new LinkedHashMap<>();
                c.put("id", rs.getInt("category_id"));
                c.put("name", rs.getString("category_name"));
                list.add(c);
            }
        }

        return list;
    }

    // THUONG HIEU DANG HOAT DONG
    public List<Map<String, Object>> getBrands()
            throws SQLException {

        String sql =
            "SELECT brand_id, brand_name " +
            "FROM Brands WHERE is_active = 1 " +
            "ORDER BY brand_name";

        List<Map<String, Object>> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> b = new LinkedHashMap<>();
                b.put("id", rs.getInt("brand_id"));
                b.put("name", rs.getString("brand_name"));
                list.add(b);
            }
        }

        return list;
    }

    // THEM SAN PHAM + PHIEN BAN + TON KHO + ANH
    // TAT CA CUNG MOT TRANSACTION
    public int addProduct(
            int categoryId,
            int brandId,
            String sku,
            String productName,
            String description,
            BigDecimal price,
            int stock,
            int warrantyMonths,
            String imageUrl
    ) throws SQLException {

        if (categoryId <= 0 || brandId <= 0 ||
            sku == null || sku.trim().isEmpty() ||
            productName == null || productName.trim().isEmpty() ||
            price == null || price.signum() < 0 ||
            stock < 0 || warrantyMonths < 0) {
            throw new IllegalArgumentException("Invalid product data");
        }
        if (sku.trim().length() > 60 || productName.trim().length() > 200
                || warrantyMonths > Short.MAX_VALUE || (imageUrl != null && imageUrl.trim().length() > 500)) {
            throw new IllegalArgumentException("Thông tin sản phẩm vượt giới hạn cho phép");
        }

        boolean oldAutoCommit = connection.getAutoCommit();

        try {
            connection.setAutoCommit(false);

            int productId;

            String productSql =
                "INSERT INTO Products " +
                "(category_id, brand_id, sku, product_name, " +
                "description, base_price, warranty_months) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps = connection.prepareStatement(
                    productSql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, categoryId);
                ps.setInt(2, brandId);
                ps.setString(3, sku.trim());
                ps.setString(4, productName.trim());
                ps.setString(5, description);
                ps.setBigDecimal(6, price);
                ps.setInt(7, warrantyMonths);

                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Cannot create product");
                    }
                    productId = keys.getInt(1);
                }
            }

            int variantId;

            String variantSql =
                "INSERT INTO ProductVariants " +
                "(product_id, variant_sku, variant_name, price) " +
                "VALUES (?, ?, ?, ?)";

            try (PreparedStatement ps = connection.prepareStatement(
                    variantSql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, productId);
                ps.setString(2, sku.trim() + "-STD");
                ps.setString(3, "Phiên bản tiêu chuẩn");
                ps.setBigDecimal(4, price);

                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Cannot create variant");
                    }
                    variantId = keys.getInt(1);
                }
            }

            String inventorySql =
                "INSERT INTO Inventory " +
                "(variant_id, quantity_available) VALUES (?, ?)";

            try (PreparedStatement ps =
                    connection.prepareStatement(inventorySql)) {
                ps.setInt(1, variantId);
                ps.setInt(2, stock);
                ps.executeUpdate();
            }

            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                String imageSql =
                    "INSERT INTO ProductImages " +
                    "(product_id, image_url, is_primary, sort_order) " +
                    "VALUES (?, ?, 1, 0)";

                try (PreparedStatement ps =
                        connection.prepareStatement(imageSql)) {
                    ps.setInt(1, productId);
                    ps.setString(2, imageUrl.trim());
                    ps.executeUpdate();
                }
            }

            connection.commit();
            return productId;

        } catch (SQLException | RuntimeException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
            }
            throw e;

        } finally {
            connection.setAutoCommit(oldAutoCommit);
        }
    }

    // SUA THONG TIN SAN PHAM
    public boolean updateProduct(
            int productId,
            int categoryId,
            int brandId,
            String sku,
            String name,
            String description,
            BigDecimal basePrice,
            int warrantyMonths
    ) throws SQLException {

        if (productId <= 0 || categoryId <= 0 || brandId <= 0 ||
            sku == null || sku.trim().isEmpty() ||
            name == null || name.trim().isEmpty() ||
            basePrice == null || basePrice.signum() < 0 ||
            warrantyMonths < 0) {
            throw new IllegalArgumentException("Invalid product data");
        }

        String sql =
            "UPDATE Products SET " +
            "category_id = ?, brand_id = ?, sku = ?, " +
            "product_name = ?, description = ?, base_price = ?, " +
            "warranty_months = ?, updated_at = SYSUTCDATETIME() " +
            "WHERE product_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            ps.setInt(2, brandId);
            ps.setString(3, sku.trim());
            ps.setString(4, name.trim());
            ps.setString(5, description);
            ps.setBigDecimal(6, basePrice);
            ps.setInt(7, warrantyMonths);
            ps.setInt(8, productId);

            return ps.executeUpdate() > 0;
        }
    }

    // SUA GIA, SKU, TEN VA TRANG THAI PHIEN BAN
    public boolean updateVariant(
            int productId,
            int variantId,
            String variantSku,
            String variantName,
            String color,
            BigDecimal price,
            boolean active
    ) throws SQLException {

        if (productId <= 0 || variantId <= 0 ||
            variantSku == null || variantSku.trim().isEmpty() ||
            variantName == null || variantName.trim().isEmpty() ||
            price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Invalid variant data");
        }
        if (variantSku.trim().length() > 80 || variantName.trim().length() > 120
                || (color != null && color.length() > 60)) {
            throw new IllegalArgumentException("Thông tin phiên bản vượt giới hạn cho phép");
        }

        String sql =
            "UPDATE ProductVariants SET " +
            "variant_sku = ?, variant_name = ?, color = ?, " +
            "price = ?, is_active = ? " +
            "WHERE variant_id = ? AND product_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, variantSku.trim());
            ps.setString(2, variantName.trim());
            ps.setString(3, color);
            ps.setBigDecimal(4, price);
            ps.setBoolean(5, active);
            ps.setInt(6, variantId);
            ps.setInt(7, productId);

            return ps.executeUpdate() > 0;
        }
    }

    // DIEU CHINH TON KHO
    public boolean updateStock(
            int productId,
            int variantId,
            int quantity
    ) throws SQLException {

        if (productId <= 0 || variantId <= 0 || quantity < 0) {
            throw new IllegalArgumentException("Invalid stock");
        }

        String sql =
            "UPDATE i SET quantity_available = ?, " +
            "updated_at = SYSUTCDATETIME() " +
            "FROM Inventory i " +
            "JOIN ProductVariants v " +
            "ON v.variant_id = i.variant_id " +
            "WHERE i.variant_id = ? AND v.product_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, variantId);
            ps.setInt(3, productId);

            return ps.executeUpdate() > 0;
        }
    }

    // BAT / TAT SAN PHAM
    // KHONG XOA VAT LY DE GIU LICH SU DON HANG
    public boolean setProductStatus(int productId, boolean active)
            throws SQLException {

        String sql =
            "UPDATE Products SET status = ?, " +
            "updated_at = SYSUTCDATETIME() " +
            "WHERE product_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, active ? "ACTIVE" : "INACTIVE");
            ps.setInt(2, productId);

            return ps.executeUpdate() > 0;
        }
    }

    // THEM HOAC THAY ANH DAI DIEN
    public void setPrimaryImage(int productId, String imageUrl)
            throws SQLException {

        if (productId <= 0 || imageUrl == null ||
            imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid image");
        }

        boolean oldAutoCommit = connection.getAutoCommit();

        try {
            connection.setAutoCommit(false);

            String updateSql =
                "UPDATE ProductImages SET is_primary = 0 " +
                "WHERE product_id = ?";

            try (PreparedStatement ps =
                    connection.prepareStatement(updateSql)) {
                ps.setInt(1, productId);
                ps.executeUpdate();
            }

            String insertSql =
                "INSERT INTO ProductImages " +
                "(product_id, image_url, is_primary, sort_order) " +
                "VALUES (?, ?, 1, 0)";

            try (PreparedStatement ps =
                    connection.prepareStatement(insertSql)) {
                ps.setInt(1, productId);
                ps.setString(2, imageUrl.trim());
                ps.executeUpdate();
            }

            connection.commit();

        } catch (SQLException | RuntimeException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackError) {
                e.addSuppressed(rollbackError);
            }
            throw e;

        } finally {
            connection.setAutoCommit(oldAutoCommit);
        }
    }
}
