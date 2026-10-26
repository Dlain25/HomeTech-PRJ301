package controller;

import dal.ProductDAO;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import model.Product;

@WebServlet(name = "PublicHome", urlPatterns = "/home")
public class HomeServlet extends HttpServlet {
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        PublicView.initialize(req, "home");
        try {
            PublicView.categories(req);
            ProductDAO dao = new ProductDAO();
            List<Product> featured = dao.featured(), newest = dao.newest();
            PublicView.images(req, featured); PublicView.images(req, newest);
            req.setAttribute("featuredProducts", featured); req.setAttribute("newProducts", newest);
        } catch (SQLException ex) { PublicView.unavailable(req, res, ex); }
        req.getRequestDispatcher("/views/public/home.jsp").forward(req, res);
    }
}

