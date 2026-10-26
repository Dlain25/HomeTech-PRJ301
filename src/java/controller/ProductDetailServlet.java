package controller;

import dal.ProductDAO;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import model.*;

@WebServlet(name = "PublicProductDetail", urlPatterns = "/product")
public class ProductDetailServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        PublicView.initialize(req, "products");
        int id;
        try {
            id = Integer.parseInt(req.getParameter("id"));
            if (id <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            req.setAttribute("productNotFound", true); res.setStatus(404);
            req.getRequestDispatcher("/views/public/product-detail.jsp").forward(req, res); return;
        }
        try {
            PublicView.categories(req);
            ProductDAO dao = new ProductDAO();
            Product product = dao.findActive(id);
            if (product == null) { req.setAttribute("productNotFound", true); res.setStatus(404); }
            else {
                PublicView.images(req, List.of(product));
                List<ProductVariant> variants = dao.variants(id);
                List<ProductImage> images = dao.images(id);
                for (ProductImage i : images) i.setImageUrl(PublicView.imageUrl(req.getContextPath(), i.getImageUrl()));
                List<Product> related = dao.related(product.getCategoryId(), id);
                PublicView.images(req, related);
                req.setAttribute("product", product); req.setAttribute("variants", variants);
                req.setAttribute("selectedVariant", variants.isEmpty() ? null : variants.get(0));
                req.setAttribute("productImages", images); req.setAttribute("relatedProducts", related);
            }
        } catch (SQLException ex) { PublicView.unavailable(req, res, ex); }
        req.getRequestDispatcher("/views/public/product-detail.jsp").forward(req, res);
    }
}

