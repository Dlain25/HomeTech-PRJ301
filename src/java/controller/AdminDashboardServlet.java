
package controller;

import dal.DashboardDAO;
import model.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.math.BigDecimal;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "AdminDashboardServlet",
            urlPatterns = {"/admin/dashboard"})
public class AdminDashboardServlet extends HttpServlet {

    private final DashboardDAO dao = new DashboardDAO();

    private boolean checkAdmin(HttpServletRequest request,
                               HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Admin access required"
            );
            return false;
        }

        Object obj = session.getAttribute("user");

        if (!(obj instanceof User)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Admin access required"
            );
            return false;
        }

        User user = (User) obj;

        // Kiểm tra quyền hiện tại từ database
        try {
            User current = new dal.UserDAO().getById(user.getUserId());

            if (current == null
                    || current.getRoleId() != 1
                    || !"ACTIVE".equals(current.getStatus())) {

                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Admin access required"
                );
                return false;
            }

        } catch (SQLException e) {
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Cannot verify admin access"
            );
            return false;
        }

        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        if (!checkAdmin(request, response)) {
            return;
        }

        try {
            int totalProducts = dao.getTotalProducts();
            int totalUsers = dao.getTotalUsers();
            int totalOrders = dao.getTotalOrders();

            BigDecimal totalRevenue = dao.getTotalRevenue();

            List<Object[]> monthlyRevenue =
                    dao.getMonthlyRevenue();

            List<Object[]> topSellingProducts =
                    dao.getTopSellingProducts();

            request.setAttribute("totalProducts", totalProducts);
            request.setAttribute("totalUsers", totalUsers);
            request.setAttribute("totalOrders", totalOrders);
            request.setAttribute("totalRevenue", totalRevenue);
            request.setAttribute("monthlyRevenue", monthlyRevenue);
            request.setAttribute("topSellingProducts",
                                 topSellingProducts);

            request.getRequestDispatcher(
                    "/views/admin/dashboard.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            throw new ServletException(
                    "Cannot load admin dashboard", e
            );
        }
    }
}
