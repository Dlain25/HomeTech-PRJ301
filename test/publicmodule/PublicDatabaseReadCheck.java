package publicmodule;

import dal.*;
import model.*;
import java.util.*;

/** Run ONLY after Thai Bao has prepared HomeTechDB and environment variables.
 * Read-only; does not create database/schema/rows. Expects the original six demo SKUs. */
public final class PublicDatabaseReadCheck {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    private static ProductFilter filter(String... pairs) {
        Map<String, String[]> p = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) p.put(pairs[i], new String[]{pairs[i + 1]});
        return ProductFilter.from(p);
    }
    public static void main(String[] args) throws Exception {
        ProductDAO dao = new ProductDAO();
        check(!new CategoryDAO().findActive().isEmpty(), "Active categories missing");
        check(!new BrandDAO().findActive().isEmpty(), "Active brands missing");
        for (String sku : List.of("HT-AF001", "HT-RC002", "HT-VC003", "HT-AP004", "HT-FN005", "HT-HD006")) {
            List<Product> products = dao.search(filter("q", sku));
            Product p = products.stream().filter(x -> x.getSku().equals(sku)).findFirst().orElseThrow();
            check(dao.findActive(p.getProductId()) != null, "Detail lookup");
            List<ProductVariant> variants = dao.variants(p.getProductId());
            check(!variants.isEmpty(), "Active variants missing: " + sku);
            check(variants.stream().allMatch(v -> v.getQuantityAvailable() >= 0), "Invalid stock");
            check(p.getDisplayPrice().compareTo(variants.stream().map(ProductVariant::getPrice).min(Comparator.naturalOrder()).orElseThrow()) == 0, "Listing price differs from minimum active variant");
            check(dao.count(filter("q", sku, "categoryId", "" + p.getCategoryId(), "brandId", "" + p.getBrandId(),
                    "minPrice", p.getDisplayPrice().toBigIntegerExact().toString(), "maxPrice", p.getDisplayPrice().toBigIntegerExact().toString())) == 1, "Combined search/filters");
            check(dao.related(p.getCategoryId(), p.getProductId()).stream().allMatch(r -> r.getCategoryId() == p.getCategoryId() && r.getProductId() != p.getProductId()), "Related products");
            dao.images(p.getProductId());
        }
        check(dao.count(filter("q", "' OR 1=1 --")) == 0, "Literal injection text");
        check(!dao.search(filter("q", "Nồi")).isEmpty(), "Vietnamese search");
        List<Product> ascending = dao.search(filter("sort", "priceAsc"));
        for (int i = 1; i < ascending.size(); i++) {
            if (ascending.get(i).getDisplayPrice() != null)
                check(ascending.get(i - 1).getDisplayPrice().compareTo(ascending.get(i).getDisplayPrice()) <= 0, "Price ascending");
        }
        check(dao.featured().stream().allMatch(Product::isFeatured), "Featured products");
        check(dao.findActive(Integer.MAX_VALUE) == null, "Unknown product");
        System.out.println("PASS: read-only DAO checks against the six existing demo SKUs.");
    }
}

