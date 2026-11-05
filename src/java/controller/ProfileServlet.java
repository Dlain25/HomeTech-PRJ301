

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
import util.ValidationUtil;


@WebServlet(name="ProfileServlet", urlPatterns={"/profile"})
public class ProfileServlet extends HttpServlet {
    
    private static final String VIEW = "/views/user/profile.jsp";
   

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet ProfileServlet</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet ProfileServlet at " + request.getContextPath () + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    } 

  
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
         User sessionUser = AuthUtil.requireLogin(request, response);
        if (sessionUser == null) {
            return;
        }
        try (UserDAO dao = new UserDAO()) {
            User u = dao.findById(sessionUser.getUserId());
            if (u == null) {
                request.getSession().invalidate();
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            request.setAttribute("username", u.getUsername());
            request.setAttribute("fullName", u.getFullName());
            request.setAttribute("email", u.getEmail());
            request.setAttribute("phone", u.getPhone());
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
        String fullName = value(request, "fullName");
        String email = value(request, "email").toLowerCase();
        String phone = value(request, "phone");

        Map<String, String> errors = new LinkedHashMap<>();
        putError(errors, "fullName", ValidationUtil.validateFullName(fullName));
        putError(errors, "email", ValidationUtil.validateEmail(email));
        putError(errors, "phone", ValidationUtil.validatePhone(phone));

        try (UserDAO dao = new UserDAO()) {
            if (errors.isEmpty() && dao.existsByEmailExcept(email, sessionUser.getUserId())) {
                errors.put("email", "Email đã được người khác sử dụng");
            }
            if (errors.isEmpty()) {
                if (dao.updateProfile(sessionUser.getUserId(), fullName, email, phone)) {
                    User fresh = dao.findById(sessionUser.getUserId());
                    if (fresh == null) {
                        response.sendError(503, "Cannot reload profile");
                        return;
                    }
                    fresh.setPasswordHash(null);
                    request.getSession().setAttribute("user", fresh);
                    request.getSession().setAttribute("flashSuccess", "Cập nhật thông tin thành công");
                    response.sendRedirect(request.getContextPath() + "/profile");
                    return;
                }
                errors.put("general", "Không thể cập nhật lúc này, vui lòng thử lại");
            }
        }

        request.setAttribute("errors", errors);
        request.setAttribute("username", sessionUser.getUsername());
        request.setAttribute("fullName", fullName);
        request.setAttribute("email", email);
        request.setAttribute("phone", phone);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static String value(HttpServletRequest request, String name) {
        String v = request.getParameter(name);
        return v == null ? "" : v.trim();
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
