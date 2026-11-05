package publicmodule;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import model.*;

/** UI fixtures ONLY. Compile into an isolated test deployment, never the production WAR.
 * These fixtures test JSP/JS behavior, not SQL Server or DAO integration. */
public final class PublicUiFixtureServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setAttribute("publicPage", true);
        req.setAttribute("activePage", "products");
        req.setAttribute("placeholderUrl", req.getContextPath() + "/assets/images/product-placeholder.svg");
        res.setHeader("X-HomeTech-Test-Fixture", "true");
        Category c = new Category(); c.setCategoryId(1); c.setCategoryName("Thiết bị nhà bếp"); c.setDescription("Danh mục kiểm thử giao diện");
        Brand b = new Brand(); b.setBrandId(1); b.setBrandName("Philips");
        req.setAttribute("categories", List.of(c)); req.setAttribute("brands", List.of(b));
        Product p = new Product(); p.setProductId(1); p.setCategoryId(1); p.setBrandId(1);
        p.setProductName("Nồi chiên 5L — KIỂM THỬ UI"); p.setBrandName(b.getBrandName()); p.setCategoryName(c.getCategoryName());
        p.setSku("UI-TEST-001"); p.setBasePrice(new BigDecimal("1790000")); p.setDisplayPrice(new BigDecimal("1790000"));
        p.setWarrantyMonths(12); p.setFeatured(true);
        p.setDescription("Dữ liệu kiểm thử JSP, không phải dữ liệu HomeTechDB.\n<script>alert('xss')</script>");
        p.setImageUrl(req.getContextPath() + "/assets/images/missing-test-image.png");
        ProductVariant v1 = new ProductVariant(); v1.setVariantId(101); v1.setProductId(1); v1.setVariantSku("UI-STD");
        v1.setVariantName("Tiêu chuẩn"); v1.setColor("Trắng"); v1.setPrice(new BigDecimal("1790000")); v1.setQuantityAvailable(20);
        if ("largePrice".equals(req.getParameter("boundary"))) {
            v1.setPrice(new BigDecimal("9007199254740993.00"));
        }
        ProductVariant v2 = new ProductVariant(); v2.setVariantId(102); v2.setProductId(1); v2.setVariantSku("UI-PRO");
        v2.setVariantName("Cao cấp"); v2.setColor("Đen"); v2.setPrice(new BigDecimal("2190000")); v2.setQuantityAvailable(0);
        ProductImage image = new ProductImage(); image.setImageUrl(p.getImageUrl()); image.setPrimary(true);
        ProductImage secondImage = new ProductImage(); secondImage.setImageUrl(req.getContextPath() + "/assets/images/hero-home.svg");
        String view = switch (String.valueOf(req.getParameter("view"))) {
            case "home" -> "home";
            case "products" -> "products";
            default -> "product-detail";
        };
        boolean empty = "true".equals(req.getParameter("empty"));
        req.setAttribute("featuredProducts", empty ? List.of() : List.of(p));
        req.setAttribute("newProducts", empty ? List.of() : List.of(p));
        req.setAttribute("product", p);
        req.setAttribute("variants", empty ? List.of() : List.of(v1, v2));
        req.setAttribute("selectedVariant", empty ? null : v1);
        req.setAttribute("productImages", List.of(image, secondImage));
        req.setAttribute("relatedProducts", List.of());
        ProductFilter filter = ProductFilter.from(req.getParameterMap());
        req.setAttribute("filter", filter);
        req.setAttribute("categoryUnavailable", filter.getCategoryId() != null && filter.getCategoryId() != 1);
        req.setAttribute("brandUnavailable", filter.getBrandId() != null && filter.getBrandId() != 1);
        req.setAttribute("products", empty ? List.of() : List.of(p));
        req.setAttribute("totalProducts", empty ? 0 : 7);
        req.setAttribute("totalPages", empty ? 1 : 2);
        req.setAttribute("pageLinks", List.of(Map.of("number", 1, "url", req.getContextPath() + "/products?" + filter.queryForPage(1)), Map.of("number", 2, "url", req.getContextPath() + "/products?" + filter.queryForPage(2))));
        req.setAttribute("previousUrl", req.getContextPath() + "/products?" + filter.queryForPage(1));
        req.setAttribute("nextUrl", req.getContextPath() + "/products?" + filter.queryForPage(2));
        req.getRequestDispatcher("/views/public/" + view + ".jsp").forward(req, res);
    }
}

