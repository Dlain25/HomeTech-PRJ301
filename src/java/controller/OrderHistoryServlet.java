
package controller;

import dal.OrderDAO;
import java.io.IOException;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Order;
import model.User;
import util.AuthUtil;

@WebServlet(name = "OrderHistoryServlet", urlPatterns = {"/orders"})
public class OrderHistoryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        User user = AuthUtil.requireLogin(request, response);

        if (user == null) {
            return;
        }

        String idParam = request.getParameter("id");

        try (OrderDAO dao = new OrderDAO()) {

            // Xem lịch sử đơn hàng
            if (idParam == null || idParam.trim().isEmpty()) {

                request.setAttribute(
                        "orders",
                        dao.findByUser(user.getUserId())
                );

                request.getRequestDispatcher(
                        "/views/user/order-history.jsp"
                ).forward(request, response);

                return;
            }

            // Xem chi tiết một đơn hàng
            long id;

            try {
                id = Long.parseLong(idParam.trim());
            } catch (NumberFormatException e) {
                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND
                );
                return;
            }

            Order order = dao.findByIdAndUser(
                    id,
                    user.getUserId()
            );

            if (order == null) {
                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND
                );
                return;
            }

            request.setAttribute("order", order);

            request.getRequestDispatcher(
                    "/views/user/order-detail.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            throw new ServletException(
                    "Cannot load order information", e
            );
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        User user = AuthUtil.requireLogin(request, response);

        if (user == null) {
            return;
        }

        request.setCharacterEncoding("UTF-8");

        long orderId;

        try {
            orderId = Long.parseLong(
                    request.getParameter("orderId").trim()
            );
        } catch (Exception e) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );
            return;
        }

        String action = request.getParameter("action");

        if (!"cancel".equals(action)) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid action"
            );
            return;
        }

        try (OrderDAO dao = new OrderDAO()) {

            dao.cancelOrder(orderId, user.getUserId());

            request.getSession().setAttribute(
                    "flashSuccess",
                    "Đã hủy đơn hàng #" + orderId
            );

        } catch (OrderDAO.OrderException e) {

            request.getSession().setAttribute(
                    "flashError",
                    e.getMessage()
            );

        } catch (SQLException e) {
            throw new ServletException(
                    "Cannot cancel order", e
            );
        }

        response.sendRedirect(
                request.getContextPath() + "/orders?id=" + orderId
        );
    }
}
