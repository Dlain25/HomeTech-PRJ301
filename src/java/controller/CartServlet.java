
package controller;

import dal.CartDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import model.CartItem;
import model.User;
import util.AuthUtil;
import java.sql.SQLException;

@WebServlet(name="CartServlet", urlPatterns={"/cart"})
public class CartServlet extends HttpServlet {
   
  
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet CartServlet</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet CartServlet at " + request.getContextPath () + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    } 

  
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
         User user = AuthUtil.requireLogin(request, response);
        if (user == null) {
            return;
        }
        List<CartItem> items;
        try (CartDAO dao = new CartDAO()) {
    items = dao.getItems(user.getUserId());
} catch (SQLException e) {
    throw new ServletException("Cannot load cart", e);
}
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem it : items) {
            total = total.add(it.getLineTotal());
        }
        request.setAttribute("items", items);
        request.setAttribute("total", total);
        request.getRequestDispatcher("/views/user/cart.jsp").forward(request, response);
    } 


@Override
protected void doPost(HttpServletRequest request,
                      HttpServletResponse response)
        throws ServletException, IOException {

    User user = AuthUtil.requireLogin(request, response);
    if (user == null) {
        return;
    }

    String action = request.getParameter("action");
    action = action == null ? "" : action;

    String error = null;
    String success = null;

    try (CartDAO dao = new CartDAO()) {

        switch (action) {

            case "add":
                error = dao.addItem(
                        user.getUserId(),
                        toInt(request.getParameter("variantId")),
                        toInt(request.getParameter("quantity"))
                );
                success = "Đã thêm sản phẩm vào giỏ hàng";
                break;

            case "update":
                error = dao.updateQuantity(
                        user.getUserId(),
                        toInt(request.getParameter("cartItemId")),
                        toInt(request.getParameter("quantity"))
                );
                success = "Đã cập nhật giỏ hàng";
                break;

            case "remove":
                if (dao.removeItem(
                        user.getUserId(),
                        toInt(request.getParameter("cartItemId")))) {

                    success = "Đã xóa sản phẩm khỏi giỏ hàng";

                } else {
                    error = "Không tìm thấy sản phẩm trong giỏ";
                }
                break;

            default:
                error = "Yêu cầu không hợp lệ";
        }

    } catch (SQLException e) {
        throw new ServletException("Cannot update cart", e);
    }

    HttpSession session = request.getSession();

    if (error != null) {
        session.setAttribute("flashError", error);
    } else {
        session.setAttribute("flashSuccess", success);
    }

    response.sendRedirect(request.getContextPath() + "/cart");
}

    private static int toInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return -1;
        }
    }
 

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
