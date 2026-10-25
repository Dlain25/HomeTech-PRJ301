
package controller;

import dal.UserDAO;
import model.User;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "AdminUserServlet", urlPatterns = {"/admin/users"})
public class AdminUserServlet extends HttpServlet {

    private final UserDAO dao = new UserDAO();

    private User getAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object currentUser = session.getAttribute("user");

        if (!(currentUser instanceof User)) {
            return null;
        }

        User user = (User) currentUser;

        if (user.getRoleId() != 1
                || !"ACTIVE".equals(user.getStatus())) {
            return null;
        }

        return user;
    }

    private boolean checkAdmin(HttpServletRequest request,
                               HttpServletResponse response)
            throws IOException {

        if (getAdmin(request) == null) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Admin access required"
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

        String keyword = request.getParameter("keyword");

        try {
            List<User> users;

            if (keyword == null || keyword.trim().isEmpty()) {
                users = dao.getAll();
            } else {
                users = dao.search(keyword.trim());
            }

            request.setAttribute("users", users);
            request.setAttribute("keyword", keyword);

            request.getRequestDispatcher("/views/admin/users.jsp")
                   .forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Cannot load users", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        if (!checkAdmin(request, response)) {
            return;
        }

        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        try {
            int userId = Integer.parseInt(
                    request.getParameter("userId")
            );

            User admin = getAdmin(request);

            if (userId == admin.getUserId()) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "You cannot change your own account"
                );
                return;
            }

            if ("status".equals(action)) {

                String status = request.getParameter("status");

                dao.updateStatus(userId, status);

            } else if ("role".equals(action)) {

                int roleId = Integer.parseInt(
                        request.getParameter("roleId")
                );

                dao.updateRole(userId, roleId);

            } else {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid action"
                );
                return;
            }

            response.sendRedirect(
                    request.getContextPath() + "/admin/users"
            );

        }  catch (IllegalArgumentException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid input"
            );
        } catch (SQLException e) {
            throw new ServletException("Cannot update user", e);
        }
    }
}
