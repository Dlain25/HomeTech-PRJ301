package publicmodule;

import dal.*;
import model.ProductFilter;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

/** Captures the actual DAO statements without a database, validates qualified names
 * against the supplied SQL file, and exports batches for SQL Server PARSEONLY.
 * Neither this capture nor PARSEONLY proves data-access results. */
public final class PublicSqlAudit {
    private record Query(String name, String sql, Map<Integer, Object> bindings) { }
    private static final List<Query> queries = new ArrayList<>();
    private static String currentName;
    private static Connection connection() {
        return proxy(Connection.class, (p, m, a) -> {
            if (!m.getName().equals("prepareStatement")) return value(m.getReturnType());
            String sql = (String) a[0];
            Map<Integer, Object> bindings = new TreeMap<>();
            return proxy(PreparedStatement.class, (sp, sm, sa) -> {
                switch (sm.getName()) {
                    case "setNString", "setObject" -> bindings.put((Integer) sa[0], sa[1]);
                    case "executeQuery" -> {
                        queries.add(new Query(currentName, sql, new TreeMap<>(bindings)));
                        return proxy(ResultSet.class, (rp, rm, ra) -> value(rm.getReturnType()));
                    }
                }
                return value(sm.getReturnType());
            });
        });
    }
    private static Object value(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        return null;
    }
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
    private static ProductFilter filter(String... pairs) {
        Map<String, String[]> parameters = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) parameters.put(pairs[i], new String[]{pairs[i + 1]});
        return ProductFilter.from(parameters);
    }
    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]);
        ProductDAO dao = new ProductDAO(PublicSqlAudit::connection);
        currentName = "count-default"; dao.count(filter());
        ProductFilter combined = filter("q", "nồi' %_[~", "categoryId", "1", "brandId", "1",
                "minPrice", "100", "maxPrice", "9999999999999999", "sort", "priceAsc", "page", "2");
        currentName = "count-filtered"; dao.count(combined);
        currentName = "search-all-filters-ascending"; dao.search(combined);
        currentName = "search-descending"; dao.search(filter("sort", "priceDesc"));
        currentName = "search-newest"; dao.search(filter("sort", "newest"));
        currentName = "featured"; dao.featured();
        currentName = "newest"; dao.newest();
        currentName = "find-active"; dao.findActive(1);
        currentName = "related"; dao.related(1, 2);
        currentName = "variants-inventory"; dao.variants(1);
        currentName = "images"; dao.images(1);
        currentName = "categories"; new CategoryDAO(PublicSqlAudit::connection).findActive();
        currentName = "brands"; new BrandDAO(PublicSqlAudit::connection).findActive();

        String schema = Files.readString(root.resolve("PRJ301_G4.sql"), StandardCharsets.UTF_8);
        Map<String, Map<String, String>> tables = new LinkedHashMap<>();
        Matcher table = Pattern.compile("(?s)CREATE TABLE dbo\\.(\\w+) \\((.*?)\\r?\\n\\);").matcher(schema);
        while (table.find()) {
            Map<String, String> columns = new LinkedHashMap<>();
            Matcher column = Pattern.compile("(?m)^\\s+(\\w+)\\s+((?:N?VARCHAR|DECIMAL|DATETIME2)\\([^)]*\\)|INT|BIGINT|BIT|SMALLINT|TINYINT)\\b?").matcher(table.group(2));
            while (column.find()) columns.put(column.group(1), column.group(2));
            tables.put(table.group(1), columns);
        }
        StringBuilder exported = new StringBuilder("-- Syntax-only review of captured DAO SQL. No statements execute.\nSET PARSEONLY ON;\nGO\n");
        int references = 0;
        for (Query q : queries) {
            Map<String, String> aliases = new HashMap<>();
            Matcher alias = Pattern.compile("(?:FROM|JOIN) dbo\\.(\\w+) (\\w+)", Pattern.CASE_INSENSITIVE).matcher(q.sql());
            while (alias.find()) {
                if (!tables.containsKey(alias.group(1))) throw new AssertionError("Unknown table: " + alias.group(1));
                if (!alias.group(2).equalsIgnoreCase("WHERE")) aliases.put(alias.group(2), alias.group(1));
            }
            Matcher column = Pattern.compile("\\b(\\w+)\\.(\\w+)").matcher(q.sql());
            while (column.find()) {
                String target = aliases.get(column.group(1));
                if (target == null) continue; // dbo table names and computed APPLY aliases.
                if (!tables.get(target).containsKey(column.group(2)))
                    throw new AssertionError(q.name() + ": unknown column " + target + "." + column.group(2));
                references++;
            }
            // Category/brand statements use unqualified projected columns.
            if (q.name().equals("categories") || q.name().equals("brands")) {
                String target = q.name().equals("categories") ? "Categories" : "Brands";
                for (String name : q.sql().substring(7, q.sql().indexOf(" FROM")).split(",")) {
                    if (!tables.get(target).containsKey(name.strip())) throw new AssertionError("Unknown projection " + name);
                    references++;
                }
                if (!tables.get(target).containsKey("is_active")) throw new AssertionError("Missing is_active");
            }
            long placeholders = q.sql().chars().filter(ch -> ch == '?').count();
            if (placeholders != q.bindings().size()) throw new AssertionError("Unbound parameters: " + q.name());
            exported.append("-- ").append(q.name()).append('\n');
            for (Map.Entry<Integer, Object> p : q.bindings().entrySet()) {
                String type = p.getValue() instanceof String ? "nvarchar(4000)"
                        : p.getValue() instanceof java.math.BigDecimal ? "decimal(18,2)"
                        : p.getValue() instanceof Long ? "bigint" : "int";
                exported.append("DECLARE @p").append(p.getKey()).append(' ').append(type).append(";\n");
            }
            String sql = q.sql();
            for (int i = 1; i <= placeholders; i++) sql = sql.replaceFirst("\\?", "@p" + i);
            exported.append(sql).append(";\nGO\n");
        }
        exported.append("SET PARSEONLY OFF;\nGO\n");
        Path output = root.resolve("build/review");
        Files.createDirectories(output);
        Files.writeString(output.resolve("dao-parseonly.sql"), exported, StandardCharsets.UTF_8);
        StringBuilder types = new StringBuilder();
        for (String name : List.of("Products", "Categories", "Brands", "ProductImages", "ProductVariants", "Inventory")) {
            types.append(name).append(": ").append(tables.get(name)).append('\n');
        }
        Files.writeString(output.resolve("schema-types.txt"), types, StandardCharsets.UTF_8);
        System.out.println("PASS: " + queries.size() + " captured DAO queries; " + references
                + " column references match PRJ301_G4.sql; all placeholders bound.");
        System.out.println("SQL Server parsing and real database execution remain separate checks.");
    }
}

