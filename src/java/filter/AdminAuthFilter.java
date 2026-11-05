
package filter;

import dal.UserDAO;
import model.User;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter(urlPatterns = {"/admin/*"})
public class AdminAuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig)
            throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req =
                (HttpServletRequest) request;

        HttpServletResponse resp =
                (HttpServletResponse) response;

        HttpSession session = req.getSession(false);

        // Chưa đăng nhập
        if (session == null) {
            resp.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Admin access required"
            );
            return;
        }

        Object obj = session.getAttribute("user");

        // Session không có User hợp lệ
        if (!(obj instanceof User)) {
            resp.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Admin access required"
            );
            return;
        }

        User sessionUser = (User) obj;

        try {
            // Lấy quyền mới nhất từ database
            User currentUser = new UserDAO()
                    .getById(sessionUser.getUserId());

            if (currentUser == null
                    || currentUser.getRoleId() != 1
                    || !"ACTIVE".equals(currentUser.getStatus())) {

                resp.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Admin access required"
                );
                return;
            }

            // Có quyền Admin: cho phép truy cập
            currentUser.setPasswordHash(null);
            session.setAttribute("user", currentUser);
            chain.doFilter(request, response);

        } catch (SQLException e) {
            throw new ServletException(
                    "Cannot verify admin permissions", e
            );
        }
    }

    @Override
    public void destroy() {
    }
}
