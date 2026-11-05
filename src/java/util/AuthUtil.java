package util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import dal.UserDAO;
import model.User;

public class AuthUtil {

    public static User getUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (User) session.getAttribute("user");
    }

    public static User requireLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = getUser(request);
        if (user != null) {
            try {
                user = new UserDAO().getById(user.getUserId());
                if (user == null || !"ACTIVE".equals(user.getStatus())) {
                    request.getSession().invalidate();
                    user = null;
                } else {
                    user.setPasswordHash(null);
                    request.getSession().setAttribute("user", user);
                }
            } catch (SQLException e) {
                response.sendError(503, "Cannot verify account at this time");
                return null;
            }
        }
        if (user == null) {
            if ("GET".equalsIgnoreCase(request.getMethod())) {
                String path = request.getRequestURI().substring(request.getContextPath().length());
                String query = request.getQueryString();
                request.getSession().setAttribute("redirectAfterLogin",
                        query == null ? path : path + "?" + query);
            }
            response.sendRedirect(request.getContextPath() + "/login");
        }
        return user;
    }
}
