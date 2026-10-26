package dal;

import java.sql.*;
import java.util.*;
import model.*;

public class ProductDAO {
    private final PublicConnectionProvider connections;
    // APPLY returns at most one row per product, preventing duplicate cards/counts.
    private static final String FROM = """
         FROM dbo.Products p
         JOIN dbo.Categories c ON c.category_id = p.category_id
         JOIN dbo.Brands b ON b.brand_id = p.brand_id
         OUTER APPLY (SELECT MIN(v.price) AS display_price FROM dbo.ProductVariants v
                      WHERE v.product_id = p.product_id AND v.is_active = 1) prices
         OUTER APPLY (SELECT TOP (1) pi.image_url FROM dbo.ProductImages pi
                      WHERE pi.product_id = p.product_id
                      ORDER BY pi.is_primary DESC, pi.sort_order, pi.image_id) img
         WHERE p.status = 'ACTIVE' AND c.is_active = 1 AND b.is_active = 1
        """;
    private static final String SELECT = """
        SELECT p.product_id, p.category_id, p.brand_id, p.sku, p.product_name,
               p.description, p.base_price, p.warranty_months, p.is_featured, p.created_at,
               c.category_name, b.brand_name, prices.display_price, img.image_url
        """;
    public ProductDAO() { this(PublicDatabase::open); }
    public ProductDAO(PublicConnectionProvider connections) { this.connections = connections; }

    public long count(ProductFilter filter) throws SQLException {
        List<Object> args = new ArrayList<>();
        String sql = "SELECT COUNT_BIG(*)" + FROM + conditions(filter, args);
        try (Connection c = connections.open(); PreparedStatement s = prepare(c, sql, args); ResultSet r = s.executeQuery()) {
            r.next(); return r.getLong(1);
        }
    }
    public List<Product> search(ProductFilter filter) throws SQLException {
        List<Object> args = new ArrayList<>();
        String sql = SELECT + FROM + conditions(filter, args) + orderBy(filter.getSort())
                + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        args.add((long) (filter.getPage() - 1) * ProductFilter.PAGE_SIZE); args.add(ProductFilter.PAGE_SIZE);
        return products(sql, args);
    }
    public List<Product> featured() throws SQLException {
        return products(SELECT + FROM + " AND p.is_featured = 1 ORDER BY p.created_at DESC, p.product_id DESC OFFSET 0 ROWS FETCH NEXT 4 ROWS ONLY", List.of());
    }
    public List<Product> newest() throws SQLException {
        return products(SELECT + FROM + " ORDER BY p.created_at DESC, p.product_id DESC OFFSET 0 ROWS FETCH NEXT 4 ROWS ONLY", List.of());
    }
    public Product findActive(int id) throws SQLException {
        List<Product> items = products(SELECT + FROM + " AND p.product_id = ?", List.of(id));
        return items.isEmpty() ? null : items.get(0);
    }
    public List<Product> related(int categoryId, int productId) throws SQLException {
        return products(SELECT + FROM + " AND p.category_id = ? AND p.product_id <> ? ORDER BY p.is_featured DESC, p.created_at DESC, p.product_id DESC OFFSET 0 ROWS FETCH NEXT 4 ROWS ONLY", List.of(categoryId, productId));
    }
    public List<ProductVariant> variants(int productId) throws SQLException {
        String sql = """
            SELECT v.variant_id, v.product_id, v.variant_sku, v.variant_name, v.color, v.price,
                   COALESCE(i.quantity_available, 0) AS quantity_available
            FROM dbo.ProductVariants v LEFT JOIN dbo.Inventory i ON i.variant_id = v.variant_id
            WHERE v.product_id = ? AND v.is_active = 1
            ORDER BY CASE WHEN COALESCE(i.quantity_available, 0) > 0 THEN 0 ELSE 1 END, v.price, v.variant_id
            """;
        List<ProductVariant> items = new ArrayList<>();
        try (Connection c = connections.open(); PreparedStatement s = prepare(c, sql, List.of(productId)); ResultSet r = s.executeQuery()) {
            while (r.next()) {
                ProductVariant v = new ProductVariant();
                v.setVariantId(r.getInt("variant_id")); v.setProductId(r.getInt("product_id"));
                v.setVariantSku(r.getString("variant_sku")); v.setVariantName(r.getString("variant_name"));
                v.setColor(r.getString("color")); v.setPrice(r.getBigDecimal("price"));
                v.setQuantityAvailable(r.getInt("quantity_available")); items.add(v);
            }
        }
        return items;
    }
    public List<ProductImage> images(int productId) throws SQLException {
        List<ProductImage> items = new ArrayList<>();
        String sql = "SELECT image_url, is_primary, sort_order FROM dbo.ProductImages WHERE product_id = ? ORDER BY is_primary DESC, sort_order, image_id";
        try (Connection c = connections.open(); PreparedStatement s = prepare(c, sql, List.of(productId)); ResultSet r = s.executeQuery()) {
            while (r.next()) {
                ProductImage i = new ProductImage(); i.setImageUrl(r.getString("image_url"));
                i.setPrimary(r.getBoolean("is_primary")); i.setSortOrder(r.getInt("sort_order")); items.add(i);
            }
        }
        return items;
    }
    private static String conditions(ProductFilter f, List<Object> args) {
        StringBuilder sql = new StringBuilder();
        if (!f.getQ().isEmpty()) {
            sql.append(" AND (p.product_name LIKE ? ESCAPE '~' OR p.sku LIKE ? ESCAPE '~')");
            String q = "%" + f.getQ().replace("~", "~~").replace("%", "~%").replace("_", "~_").replace("[", "~[") + "%";
            args.add(q); args.add(q);
        }
        if (f.getCategoryId() != null) { sql.append(" AND p.category_id = ?"); args.add(f.getCategoryId()); }
        if (f.getBrandId() != null) { sql.append(" AND p.brand_id = ?"); args.add(f.getBrandId()); }
        if (f.getMinPrice() != null) { sql.append(" AND prices.display_price >= ?"); args.add(f.getMinPrice()); }
        if (f.getMaxPrice() != null) { sql.append(" AND prices.display_price <= ?"); args.add(f.getMaxPrice()); }
        return sql.toString();
    }
    private static String orderBy(String sort) {
        return switch (sort) {
            case "priceAsc" -> " ORDER BY CASE WHEN prices.display_price IS NULL THEN 1 ELSE 0 END, prices.display_price ASC, p.product_id DESC";
            case "priceDesc" -> " ORDER BY CASE WHEN prices.display_price IS NULL THEN 1 ELSE 0 END, prices.display_price DESC, p.product_id DESC";
            default -> " ORDER BY p.created_at DESC, p.product_id DESC";
        };
    }
    private List<Product> products(String sql, List<Object> args) throws SQLException {
        List<Product> items = new ArrayList<>();
        try (Connection c = connections.open(); PreparedStatement s = prepare(c, sql, args); ResultSet r = s.executeQuery()) {
            while (r.next()) {
                Product p = new Product();
                p.setProductId(r.getInt("product_id")); p.setCategoryId(r.getInt("category_id")); p.setBrandId(r.getInt("brand_id"));
                p.setSku(r.getString("sku")); p.setProductName(r.getString("product_name")); p.setDescription(r.getString("description"));
                p.setBasePrice(r.getBigDecimal("base_price")); p.setDisplayPrice(r.getBigDecimal("display_price"));
                p.setWarrantyMonths(r.getInt("warranty_months")); p.setFeatured(r.getBoolean("is_featured"));
                p.setCreatedAt(r.getTimestamp("created_at")); p.setCategoryName(r.getString("category_name"));
                p.setBrandName(r.getString("brand_name")); p.setImageUrl(r.getString("image_url")); items.add(p);
            }
        }
        return items;
    }
    private static PreparedStatement prepare(Connection c, String sql, List<?> args) throws SQLException {
        PreparedStatement s = c.prepareStatement(sql);
        try {
            s.setQueryTimeout(10);
            for (int i = 0; i < args.size(); i++) {
                Object value = args.get(i);
                // Use an explicit cast: NetBeans 17 compile-on-save emitted an
                // Uncompilable-code stub for the pattern variable on this setup.
                if (value instanceof String) s.setNString(i + 1, (String) value);
                else s.setObject(i + 1, value);
            }
            return s;
        } catch (SQLException ex) { s.close(); throw ex; }
    }
}

