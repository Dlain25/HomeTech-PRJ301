package controller;

import dal.UserDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import model.User;
import util.AuthUtil;
import util.PasswordUtil;
import util.ValidationUtil;

@WebServlet(name = "ChangePasswordServlet", urlPatterns = {"/change-password"})
public class ChangePasswordServlet extends HttpServlet {

    private static final String VIEW = "/views/user/change-password.jsp";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet ChangePasswordServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet ChangePasswordServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (AuthUtil.requireLogin(request, response) == null) {
            return;
        }
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User sessionUser = AuthUtil.requireLogin(request, response);
        if (sessionUser == null) {
            return;
        }
        String oldPw = raw(request, "oldPassword");
        String newPw = raw(request, "newPassword");
        String confirm = raw(request, "confirmPassword");

        Map<String, String> errors = new LinkedHashMap<>();
        try (UserDAO dao = new UserDAO()) {
            User u = dao.findById(sessionUser.getUserId());
            if (u == null || !PasswordUtil.verify(oldPw, u.getPasswordHash())) {
                errors.put("oldPassword", "Mật khẩu hiện tại không đúng");
            }
            String check = ValidationUtil.validatePassword(newPw, sessionUser.getUsername());
            if (check != null) {
                errors.put("newPassword", check);
            } else if (newPw.equals(oldPw)) {
                errors.put("newPassword", "Mật khẩu mới phải khác mật khẩu hiện tại");
            } else if (!newPw.equals(confirm)) {
                errors.put("confirmPassword", "Mật khẩu nhập lại không khớp");
            }

            if (errors.isEmpty()) {
                if (dao.updatePassword(sessionUser.getUserId(), PasswordUtil.hash(newPw))) {
                    request.getSession().setAttribute("flashSuccess", "Đổi mật khẩu thành công");
                    response.sendRedirect(request.getContextPath() + "/profile");
                    return;
                }
                errors.put("general", "Không thể đổi mật khẩu lúc này, vui lòng thử lại");
            }
        }
        request.setAttribute("errors", errors);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static String raw(HttpServletRequest request, String name) {
        String v = request.getParameter(name);
        return v == null ? "" : v;
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }

}
