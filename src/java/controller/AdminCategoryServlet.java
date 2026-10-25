package controller;

import dal.CategoryDAO;
import model.Category;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "AdminCategoryServlet", urlPatterns = {"/admin/categories"})
public class AdminCategoryServlet extends HttpServlet {

    private final CategoryDAO dao = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        try {
            List<Category> categories = dao.getAll();
            request.setAttribute("categories", categories);
            request.getRequestDispatcher("/views/admin/categories.jsp")
                   .forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Cannot load categories", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        try {
            if ("add".equals(action) || "update".equals(action)) {

                String name = request.getParameter("categoryName");
                String description = request.getParameter("description");
                boolean active = "true".equals(request.getParameter("active"));

                if (name == null || name.trim().isEmpty()
                        || name.length() > 255) {
                    response.sendError(400, "Invalid category name");
                    return;
                }

                Category c = new Category();
                c.setCategoryName(name.trim());
                c.setDescription(description);
                c.setActive(active);

                if ("add".equals(action)) {
                    dao.insert(c);
                } else {
                    c.setCategoryId(
                        Integer.parseInt(request.getParameter("categoryId"))
                    );
                    dao.update(c);
                }

            } else if ("delete".equals(action)) {

                int id = Integer.parseInt(
                    request.getParameter("categoryId")
                );

                // Soft delete: hide category instead of deleting data
                Category c = dao.getById(id);
                if (c != null) {
                    c.setActive(false);
                    dao.update(c);
                }

            } else {
                response.sendError(400, "Invalid action");
                return;
            }

            response.sendRedirect(
                request.getContextPath() + "/admin/categories"
            );

        } catch (NumberFormatException e) {
            response.sendError(400, "Invalid category ID");
        } catch (SQLException e) {
            throw new ServletException("Category operation failed", e);
        }
    }
}