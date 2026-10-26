package model;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Normalized public search input. Price filtering uses the minimum active variant price. */
public final class ProductFilter {
    public static final int PAGE_SIZE = 6;
    private String q = "";
    private Integer categoryId, brandId;
    private BigDecimal minPrice, maxPrice;
    private String sort = "newest";
    private int page = 1;
    private boolean normalized;
    public static ProductFilter from(Map<String, String[]> parameters) {
        ProductFilter f = new ProductFilter();
        String q = value(parameters, "q").strip();
        f.q = q.length() > 200 ? q.substring(0, 200) : q;
        f.normalized = q.length() > 200;
        f.categoryId = f.id(value(parameters, "categoryId"));
        f.brandId = f.id(value(parameters, "brandId"));
        f.minPrice = f.price(value(parameters, "minPrice"));
        f.maxPrice = f.price(value(parameters, "maxPrice"));
        if (f.minPrice != null && f.maxPrice != null && f.minPrice.compareTo(f.maxPrice) > 0) {
            BigDecimal temp = f.minPrice; f.minPrice = f.maxPrice; f.maxPrice = temp; f.normalized = true;
        }
        String sort = value(parameters, "sort");
        if (sort.equals("priceAsc") || sort.equals("priceDesc") || sort.equals("newest")) f.sort = sort;
        else if (!sort.isEmpty()) f.normalized = true;
        Integer page = f.id(value(parameters, "page"));
        if (page != null) f.page = page;
        return f;
    }
    private static String value(Map<String, String[]> p, String key) {
        String[] values = p.get(key);
        return values == null || values.length == 0 || values[0] == null ? "" : values[0].trim();
    }
    private Integer id(String value) {
        if (value.isEmpty()) return null;
        try { int n = Integer.parseInt(value); if (n > 0) return n; }
        catch (NumberFormatException ignored) { }
        normalized = true; return null;
    }
    private BigDecimal price(String value) {
        if (value.isEmpty()) return null;
        try {
            // Public VND bounds are whole dong; database prices remain DECIMAL(18,2).
            if (!value.matches("[0-9]{1,16}")) throw new NumberFormatException();
            return new BigDecimal(value);
        } catch (ArithmeticException | NumberFormatException ex) { normalized = true; return null; }
    }
    public String queryForPage(int number) {
        StringBuilder query = new StringBuilder("q=").append(encode(q));
        append(query, "categoryId", categoryId); append(query, "brandId", brandId);
        append(query, "minPrice", minPrice); append(query, "maxPrice", maxPrice);
        append(query, "sort", sort); append(query, "page", number);
        return query.toString();
    }
    private static void append(StringBuilder query, String name, Object value) {
        if (value != null) query.append('&').append(name).append('=').append(encode(value.toString()));
    }
    private static String encode(String s) { return URLEncoder.encode(s, StandardCharsets.UTF_8); }
    public String getQ() { return q; }
    public Integer getCategoryId() { return categoryId; }
    public Integer getBrandId() { return brandId; }
    public BigDecimal getMinPrice() { return minPrice; }
    public BigDecimal getMaxPrice() { return maxPrice; }
    public String getSort() { return sort; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = Math.max(1, page); }
    public boolean isNormalized() { return normalized; }
}

