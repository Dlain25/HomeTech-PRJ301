package controller;

import dal.UserDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import util.PasswordUtil;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private static final String VIEW = "/views/user/login.jsp";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet LoginServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet LoginServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        username = username == null ? "" : username.trim();
        password = password == null ? "" : password;

        User user;
        try (UserDAO dao = new UserDAO()) {
            user = username.isEmpty() ? null : dao.findByUsername(username);
        }

        String error = null;
        if (user == null || !PasswordUtil.verify(password, user.getPasswordHash())) {
            error = "Sai tên đăng nhập hoặc mật khẩu";
        } else if (user.isLocked()) {
            error = "Tài khoản đã bị khóa";
        }

        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("username", username);
            request.getRequestDispatcher(VIEW).forward(request, response);
            return;
        }

        user.setPasswordHash(null);
        HttpSession session = request.getSession();
        request.changeSessionId();

        String next = (String) session.getAttribute("redirectAfterLogin");
        session.removeAttribute("redirectAfterLogin");
        session.setAttribute("user", user);

        String target = request.getContextPath() + "/";
        if (next != null && next.startsWith("/") && !next.startsWith("//")) {
            target = request.getContextPath() + next;
        }
        response.sendRedirect(target);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
