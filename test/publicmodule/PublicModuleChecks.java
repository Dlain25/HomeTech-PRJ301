package controller;

import dal.*;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import model.ProductFilter;

/** Offline contract tests: never connects to SQL Server or changes data. */
public final class PublicModuleChecks {
    private static int assertions;
    private static void check(boolean value, String message) {
        assertions++; if (!value) throw new AssertionError(message);
    }
    private static ProductFilter filter(String... pairs) {
        Map<String, String[]> p = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) p.put(pairs[i], new String[]{pairs[i + 1]});
        return ProductFilter.from(p);
    }
    public static void main(String[] args) throws Exception {
        ProductFilter empty = filter();
        check(empty.getPage() == 1 && empty.getSort().equals("newest"), "Defaults");
        ProductFilter invalid = filter("categoryId", "-1", "brandId", "1 OR 1=1", "minPrice", "NaN", "maxPrice", "1e99", "page", "2147483648", "sort", "price;DROP TABLE Products");
        check(invalid.isNormalized(), "Invalid input is reported");
        check(invalid.getCategoryId() == null && invalid.getBrandId() == null, "Invalid IDs ignored");
        check(invalid.getMinPrice() == null && invalid.getMaxPrice() == null, "Invalid prices ignored");
        check(invalid.getPage() == 1 && invalid.getSort().equals("newest"), "Safe page and sort");
        ProductFilter swapped = filter("minPrice", "200", "maxPrice", "100");
        check(swapped.getMinPrice().equals(new BigDecimal("100")) && swapped.getMaxPrice().equals(new BigDecimal("200")), "Reversed whole-VND range normalized");
        for (String price : List.of("-1", "0.01", "100.00", "100,5", "1e6", "100.000đ", "abc")) {
            ProductFilter rejected = filter("minPrice", price);
            check(rejected.getMinPrice() == null && rejected.isNormalized(), "Invalid VND bound rejected: " + price);
        }
        check(filter("minPrice", "0").getMinPrice().equals(BigDecimal.ZERO), "Zero VND is valid");
        check(filter("maxPrice", "1590000").getMaxPrice().equals(new BigDecimal("1590000")), "Typed whole amount need not be a multiple of the spinner step");
        check(filter("maxPrice", "9999999999999999").getMaxPrice().equals(new BigDecimal("9999999999999999")), "Largest whole DECIMAL(18,2) bound remains exact");
        check(filter("minPrice", "99999999999999999").getMinPrice() == null, "SQL decimal overflow rejected");
        check(filter("q", "x".repeat(300)).getQ().length() == 200, "Search bounded");
        String attack = "nồi' OR 1=1 -- %_[~";
        ProductFilter combined = filter("q", attack, "categoryId", "2", "brandId", "3", "minPrice", "100", "maxPrice", "200", "sort", "priceAsc", "page", "2");
        String query = combined.queryForPage(3);
        check(query.contains("categoryId=2") && query.contains("brandId=3") && query.contains("minPrice=100&") && query.contains("maxPrice=200&") && query.contains("sort=priceAsc") && query.contains("page=3"), "Pagination retains every filter as whole VND");
        check(query.contains("%E1%BB%93") && !query.contains(" OR "), "Unicode query safely encoded");
        check(PublicView.imageUrl("/shop", null).endsWith("product-placeholder.svg"), "Missing image");
        for (String unsafe : List.of("javascript:alert(1)", "//evil.test/a", "../private", "/%2e%2e/a", "data:image/svg+xml,test")) {
            check(PublicView.imageUrl("/shop", unsafe).endsWith("product-placeholder.svg"), "Unsafe image rejected: " + unsafe);
        }
        check(PublicView.imageUrl("/renamed", "/assets/a.png").equals("/renamed/assets/a.png"), "Context-relative image");
        check(PublicView.imageUrl("/shop", "https://example.com/a.png").equals("https://example.com/a.png"), "HTTPS image");
        Recorder recorder = new Recorder();
        ProductDAO dao = new ProductDAO(recorder::connection);
        dao.search(combined);
        check(!recorder.sql.contains(attack), "Search input never concatenated into SQL");
        check(recorder.sql.contains("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY"), "SQL pagination");
        check(recorder.sql.contains("p.status = 'ACTIVE'") && recorder.sql.contains("c.is_active = 1") && recorder.sql.contains("b.is_active = 1"), "Only active public catalog");
        check(recorder.sql.contains("MIN(v.price)") && recorder.sql.contains("v.is_active = 1"), "Listing price uses active variants");
        check(recorder.sql.contains("TOP (1)") && recorder.sql.contains("pi.is_primary DESC, pi.sort_order"), "Single prioritized image");
        check(recorder.params.get(1).equals("%nồi' OR 1=1 -- ~%~_~[~~%"), "LIKE metacharacters escaped as literal search");
        check(recorder.params.get(3).equals(2) && recorder.params.get(4).equals(3), "Combined category and brand bindings");
        check(recorder.params.get(5).equals(new BigDecimal("100")) && recorder.params.get(6).equals(new BigDecimal("200")), "Exact whole VND bounds reach JDBC");
        check(recorder.sql.contains("prices.display_price >= ?") && recorder.sql.contains("prices.display_price <= ?") && !recorder.sql.contains("p.base_price >="), "Price filter compares the active variant selling price");
        check(recorder.params.get(7).equals(6L) && recorder.params.get(8).equals(6), "Offset and limit bindings");
        check(recorder.closedConnections == 1 && recorder.closedStatements == 1 && recorder.closedResults == 1, "JDBC resources closed");
        dao.variants(7);
        check(recorder.sql.contains("LEFT JOIN dbo.Inventory") && recorder.sql.contains("COALESCE(i.quantity_available, 0)") && recorder.params.get(1).equals(7), "Variant stock and missing inventory");
        dao.related(2, 7);
        check(recorder.sql.contains("p.category_id = ? AND p.product_id <> ?"), "Related products exclude current product");
        dao.featured();
        check(recorder.sql.contains("p.is_featured = 1"), "Featured predicate");
        dao.newest();
        check(recorder.sql.contains("p.created_at DESC, p.product_id DESC"), "Stable newest ordering");
        dao.search(filter("sort", "priceDesc"));
        check(recorder.sql.contains("prices.display_price DESC"), "Price descending");
        Class<?> driver = Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        check(Driver.class.isAssignableFrom(driver), "Bundled SQL Server driver loads on Java 17");
        System.out.println("PASS: " + assertions + " offline checks. No database behavior is claimed.");
    }
    private static final class Recorder {
        String sql;
        Map<Integer, Object> params = new HashMap<>();
        int closedConnections, closedStatements, closedResults;
        Connection connection() {
            return proxy(Connection.class, (p, m, a) -> switch (m.getName()) {
                case "prepareStatement" -> { sql = (String) a[0]; params.clear(); yield statement(); }
                case "close" -> { closedConnections++; yield null; }
                default -> defaultValue(m.getReturnType());
            });
        }
        PreparedStatement statement() {
            return proxy(PreparedStatement.class, (p, m, a) -> switch (m.getName()) {
                case "setObject", "setNString" -> { params.put((Integer) a[0], a[1]); yield null; }
                case "executeQuery" -> proxy(ResultSet.class, (rp, rm, ra) -> {
                    if (rm.getName().equals("close")) closedResults++;
                    return defaultValue(rm.getReturnType());
                });
                case "close" -> { closedStatements++; yield null; }
                default -> defaultValue(m.getReturnType());
            });
        }
    }
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
    private static Object defaultValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        return null;
    }
}

