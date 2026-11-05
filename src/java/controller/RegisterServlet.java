
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
import util.PasswordUtil;
import util.ValidationUtil;

@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    private static final String VIEW = "/views/user/register.jsp";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet RegisterServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet RegisterServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = value(request, "username");
        String email = value(request, "email").toLowerCase();
        String fullName = value(request, "fullName");
        String phone = value(request, "phone");
        String password = raw(request, "password");
        String confirm = raw(request, "confirmPassword");

        Map<String, String> errors = new LinkedHashMap<>();
        putError(errors, "username", ValidationUtil.validateUsername(username));
        putError(errors, "fullName", ValidationUtil.validateFullName(fullName));
        putError(errors, "email", ValidationUtil.validateEmail(email));
        putError(errors, "phone", ValidationUtil.validatePhone(phone));
        putError(errors, "password", ValidationUtil.validatePassword(password, username));
        if (!errors.containsKey("password") && !password.equals(confirm)) {
            errors.put("confirmPassword", "Mật khẩu nhập lại không khớp");
        }

        try (UserDAO dao = new UserDAO()) {
            if (errors.isEmpty()) {
                if (dao.existsByUsername(username)) {
                    errors.put("username", "Tên đăng nhập đã tồn tại");
                }
                if (dao.existsByEmail(email)) {
                    errors.put("email", "Email đã được sử dụng");
                }
            }
            if (errors.isEmpty()) {
                User u = new User();
                u.setUsername(username);
                u.setEmail(email);
                u.setFullName(fullName);
                u.setPhone(phone);
                u.setPasswordHash(PasswordUtil.hash(password));
                int id = dao.insertCustomer(u);
                if (id > 0) {
                    request.getSession().setAttribute("flashSuccess", "Đăng ký thành công! Hãy đăng nhập.");
                    response.sendRedirect(request.getContextPath() + "/login");
                    return;
                }
                errors.put("general", "Không thể đăng ký lúc này, vui lòng thử lại");
            }
        }

        request.setAttribute("errors", errors);
        request.setAttribute("username", username);
        request.setAttribute("email", email);
        request.setAttribute("fullName", fullName);
        request.setAttribute("phone", phone);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static String value(HttpServletRequest request, String name) {
        String v = request.getParameter(name);
        return v == null ? "" : v.trim();
    }

    private static String raw(HttpServletRequest request, String name) {
        String v = request.getParameter(name);
        return v == null ? "" : v;
    }

    private static void putError(Map<String, String> errors, String field, String message) {
        if (message != null) {
            errors.put(field, message);
        }
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }

}
