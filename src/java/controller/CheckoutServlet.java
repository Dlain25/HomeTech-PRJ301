
package controller;

import dal.CartDAO;
import dal.OrderDAO;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.CartItem;
import model.User;
import util.AuthUtil;
import util.ValidationUtil;

@WebServlet(name = "CheckoutServlet", urlPatterns = {"/checkout"})
public class CheckoutServlet extends HttpServlet {

    private static final String VIEW = "/views/user/checkout.jsp";

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        User user = AuthUtil.requireLogin(request, response);
        if (user == null) {
            return;
        }

        if (!loadCart(request, response, user)) {
            return;
        }

        request.setAttribute("recipientName", user.getFullName());
        request.setAttribute("recipientPhone", user.getPhone());
        request.setAttribute("paymentMethod", "COD");

        request.getRequestDispatcher(VIEW).forward(request, response);
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

        String name = value(request, "recipientName");
        String phone = value(request, "recipientPhone");
        String address = value(request, "shippingAddress");
        String note = value(request, "note");
        String method = value(request, "paymentMethod");
        String date=value(request,"desiredDeliveryDate"),slot=value(request,"deliverySlot");
        java.time.LocalDate deliveryDate=null;

        Map<String, String> errors = new LinkedHashMap<>();
        try { deliveryDate=util.DeliveryDateUtil.parse(date,slot,java.time.Clock.systemUTC()); }
        catch (RuntimeException e) { errors.put("deliveryDate","Chọn ngày nhận từ ngày mai đến 14 ngày tới và khung giờ hợp lệ."); }
        if ("BANK_TRANSFER".equals(method) && !util.FeatureConfig.bankReady()) errors.put("paymentMethod","Chuyển khoản chưa được cấu hình. Bạn có thể chọn COD.");

        String e1 = ValidationUtil.validateFullName(name);
        if (e1 != null) {
            errors.put("recipientName", e1);
        }

        String e2 = ValidationUtil.validatePhone(phone);
        if (e2 != null) {
            errors.put("recipientPhone", e2);
        }

        if (address.isEmpty()) {
            errors.put("shippingAddress", "Vui lòng nhập địa chỉ nhận hàng");
        } else if (address.length() > 500) {
            errors.put("shippingAddress", "Địa chỉ tối đa 500 ký tự");
        }

        if (note.length() > 500) {
            errors.put("note", "Ghi chú tối đa 500 ký tự");
        }

        if (!"COD".equals(method)
                && !"BANK_TRANSFER".equals(method)) {
            errors.put("paymentMethod",
                    "Vui lòng chọn phương thức thanh toán");
        }

        if (errors.isEmpty()) {
            try (OrderDAO dao = new OrderDAO()) {

                long orderId = dao.createOrder(
                        user.getUserId(),
                        name,
                        phone,
                        address,
                        note,
                        method, deliveryDate, slot
                );

                request.getSession().setAttribute(
                        "flashSuccess",
                        "Đã tạo đơn, đang chờ duyệt. Mã đơn hàng #" + orderId
                );

                response.sendRedirect(
                        request.getContextPath() + ("BANK_TRANSFER".equals(method)?"/payment/qr?kind=order&id=":"/orders?id=") + orderId
                );
                return;

            } catch (OrderDAO.OrderException e) {
                errors.put("general", e.getMessage());

            } catch (SQLException e) {
                throw new ServletException(
                        "Cannot create order: Database connection failed", e
                );
            }
        }

        if (!loadCart(request, response, user)) {
            return;
        }

        request.setAttribute("errors", errors);
        request.setAttribute("recipientName", name);
        request.setAttribute("recipientPhone", phone);
        request.setAttribute("shippingAddress", address);
        request.setAttribute("note", note);
        request.setAttribute("paymentMethod", method);
        request.setAttribute("desiredDeliveryDate",date);
        request.setAttribute("deliverySlot",slot);

        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private boolean loadCart(HttpServletRequest request,
                             HttpServletResponse response,
                             User user)
            throws ServletException, IOException {

        java.time.LocalDate today=java.time.LocalDate.now(util.DeliveryDateUtil.ZONE);
        request.setAttribute("minDeliveryDate",today.plusDays(1));
        request.setAttribute("maxDeliveryDate",today.plusDays(14));
        request.setAttribute("suggestedDates",java.util.List.of(today.plusDays(1),today.plusDays(3),today.plusDays(7)));
        request.setAttribute("bankReady",util.FeatureConfig.bankReady());
        List<CartItem> items;

        try (CartDAO dao = new CartDAO()) {
            items = dao.getItems(user.getUserId());

        } catch (SQLException e) {
            throw new ServletException(
                    "Cannot load checkout cart", e
            );
        }

        if (items.isEmpty()) {
            request.getSession().setAttribute(
                    "flashError", "Giỏ hàng đang trống"
            );

            response.sendRedirect(
                    request.getContextPath() + "/cart"
            );
            return false;
        }

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem it : items) {
            subtotal = subtotal.add(it.getLineTotal());
        }

        request.setAttribute("items", items);
        request.setAttribute("subtotal", subtotal);
        request.setAttribute("shippingFee", OrderDAO.SHIPPING_FEE);
        request.setAttribute(
                "total",
                subtotal.add(OrderDAO.SHIPPING_FEE)
        );

        return true;
    }

    private static String value(HttpServletRequest request, String name) {
        String v = request.getParameter(name);
        return v == null ? "" : v.trim();
    }
}
