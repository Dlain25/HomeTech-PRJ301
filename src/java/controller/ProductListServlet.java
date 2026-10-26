package controller;

import dal.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import model.*;

@WebServlet(name = "PublicProductList", urlPatterns = "/products")
public class ProductListServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        PublicView.initialize(req, "products");
        ProductFilter filter = ProductFilter.from(req.getParameterMap());
        req.setAttribute("filter", filter);
        try {
            List<Category> categories = new CategoryDAO().findActive();
            req.setAttribute("categories", categories);
            List<Brand> brands = new BrandDAO().findActive();
            req.setAttribute("brands", brands);
            // Retain unavailable IDs in the form so sorting cannot silently remove a filter.
            req.setAttribute("categoryUnavailable", filter.getCategoryId() != null
                    && categories.stream().noneMatch(c -> c.getCategoryId() == filter.getCategoryId()));
            req.setAttribute("brandUnavailable", filter.getBrandId() != null
                    && brands.stream().noneMatch(b -> b.getBrandId() == filter.getBrandId()));
            ProductDAO dao = new ProductDAO();
            long count = dao.count(filter);
            int pages = (int) Math.max(1, (count + ProductFilter.PAGE_SIZE - 1) / ProductFilter.PAGE_SIZE);
            filter.setPage(Math.min(filter.getPage(), pages));
            List<Product> products = dao.search(filter);
            PublicView.images(req, products);
            req.setAttribute("products", products); req.setAttribute("totalProducts", count);
            req.setAttribute("totalPages", pages);
            List<Map<String, Object>> links = new ArrayList<>();
            for (int i = Math.max(1, filter.getPage() - 2); i <= Math.min(pages, filter.getPage() + 2); i++) {
                links.add(Map.of("number", i, "url", req.getContextPath() + "/products?" + filter.queryForPage(i)));
            }
            req.setAttribute("pageLinks", links);
            req.setAttribute("previousUrl", req.getContextPath() + "/products?" + filter.queryForPage(Math.max(1, filter.getPage() - 1)));
            req.setAttribute("nextUrl", req.getContextPath() + "/products?" + filter.queryForPage(Math.min(pages, filter.getPage() + 1)));
        } catch (SQLException ex) { PublicView.unavailable(req, res, ex); }
        req.getRequestDispatcher("/views/public/products.jsp").forward(req, res);
    }
}

