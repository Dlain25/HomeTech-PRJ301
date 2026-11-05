
package controller;

import dal.AdminProductDAO;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.UUID;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

@WebServlet(name = "AdminProductServlet",
        urlPatterns = {"/admin/products"})
public class AdminProductServlet extends HttpServlet {

    private static final String VIEW = "/views/admin/products.jsp";
    private static final String CSRF_KEY = "adminProductCsrf";

    private boolean checkAdmin(HttpServletRequest request,
                               HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null) {
            response.sendError(403, "Admin access required");
            return false;
        }

        Object obj = session.getAttribute("user");

        if (!(obj instanceof User)) {
            response.sendError(403, "Admin access required");
            return false;
        }

        User user = (User) obj;

        if (user.getRoleId() != 1
                || !"ACTIVE".equals(user.getStatus())) {

            response.sendError(403, "Admin access required");
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

        HttpSession session = request.getSession();

        String token = (String) session.getAttribute(CSRF_KEY);

        if (token == null) {
            token = UUID.randomUUID().toString();
            session.setAttribute(CSRF_KEY, token);
        }

        request.setAttribute("csrfToken", token);

        try (AdminProductDAO dao = new AdminProductDAO()) {

            request.setAttribute("products", dao.getAll());
            request.setAttribute("categories", dao.getCategories());
            request.setAttribute("brands", dao.getBrands());

            request.getRequestDispatcher(VIEW)
                    .forward(request, response);

        } catch (SQLException e) {
            throw new ServletException(
                    "Cannot load Admin Product Management", e
            );
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        if (!checkAdmin(request, response)) {
            return;
        }

        HttpSession session = request.getSession();

        String expectedToken =
                (String) session.getAttribute(CSRF_KEY);

        String submittedToken =
                request.getParameter("csrfToken");

        if (expectedToken == null
                || submittedToken == null
                || !expectedToken.equals(submittedToken)) {

            response.sendError(403, "Invalid CSRF token");
            return;
        }

        String action = request.getParameter("action");

        if (action == null) {
            response.sendError(400, "Missing action");
            return;
        }

        try (AdminProductDAO dao = new AdminProductDAO()) {

            String message;

            switch (action) {

                case "add":
                    int newId = dao.addProduct(
                            positiveInt(request, "categoryId"),
                            positiveInt(request, "brandId"),
                            required(request, "sku"),
                            required(request, "productName"),
                            value(request, "description"),
                            price(request, "price"),
                            nonNegativeInt(request, "stock"),
                            nonNegativeInt(request, "warrantyMonths"),
                            value(request, "imageUrl")
                    );

                    message = "Đã thêm sản phẩm #" + newId;
                    break;

                case "update":
                    boolean updated = dao.updateProduct(
                            positiveInt(request, "productId"),
                            positiveInt(request, "categoryId"),
                            positiveInt(request, "brandId"),
                            required(request, "sku"),
                            required(request, "productName"),
                            value(request, "description"),
                            price(request, "basePrice"),
                            nonNegativeInt(request, "warrantyMonths")
                    );

                    message = updated
                            ? "Cập nhật sản phẩm thành công"
                            : "Không tìm thấy sản phẩm";
                    break;

                case "variant":
                    boolean variantUpdated = dao.updateVariant(
                            positiveInt(request, "productId"),
                            positiveInt(request, "variantId"),
                            required(request, "variantSku"),
                            required(request, "variantName"),
                            value(request, "color"),
                            price(request, "price"),
                            "true".equals(request.getParameter("active"))
                    );

                    message = variantUpdated
                            ? "Cập nhật phiên bản thành công"
                            : "Không tìm thấy phiên bản";
                    break;

                case "stock":
                    boolean stockUpdated = dao.updateStock(
                            positiveInt(request, "productId"),
                            positiveInt(request, "variantId"),
                            nonNegativeInt(request, "stock")
                    );

                    message = stockUpdated
                            ? "Cập nhật tồn kho thành công"
                            : "Không tìm thấy phiên bản";
                    break;

                case "status":
                    int productId =
                            positiveInt(request, "productId");

                    String status = required(request, "status");

                    if (!"ACTIVE".equals(status)
                            && !"INACTIVE".equals(status)) {
                        throw new IllegalArgumentException(
                                "Trạng thái không hợp lệ"
                        );
                    }

                    boolean statusUpdated = dao.setProductStatus(
                            productId,
                            "ACTIVE".equals(status)
                    );

                    message = statusUpdated
                            ? "Đã cập nhật trạng thái sản phẩm"
                            : "Không tìm thấy sản phẩm";
                    break;

                case "image":
                    dao.setPrimaryImage(
                            positiveInt(request, "productId"),
                            required(request, "imageUrl")
                    );

                    message = "Đã cập nhật ảnh sản phẩm";
                    break;

                default:
                    response.sendError(400, "Invalid action");
                    return;
            }

            session.setAttribute("adminProductSuccess", message);

        } catch (IllegalArgumentException e) {

            session.setAttribute(
                    "adminProductError",
                    e.getMessage()
            );

        } catch (SQLException e) {

            throw new ServletException(
                    "Cannot update product", e
            );
        }

        response.sendRedirect(
                request.getContextPath() + "/admin/products"
        );
    }

    private static String value(HttpServletRequest request,
                                String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    private static String required(HttpServletRequest request,
                                   String name) {
        String result = value(request, name);

        if (result.isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập " + name
            );
        }

        int limit = switch (name) {
            case "sku" -> 60;
            case "productName" -> 200;
            case "variantSku" -> 80;
            case "variantName" -> 120;
            case "imageUrl" -> 500;
            default -> Integer.MAX_VALUE;
        };
        if (result.length() > limit) {
            throw new IllegalArgumentException(name + " tối đa " + limit + " ký tự");
        }

        return result;
    }

    private static int positiveInt(HttpServletRequest request,
                                   String name) {
        int number = parseInt(request, name);

        if (number <= 0) {
            throw new IllegalArgumentException(
                    name + " phải lớn hơn 0"
            );
        }

        return number;
    }

    private static int nonNegativeInt(HttpServletRequest request,
                                      String name) {
        int number = parseInt(request, name);

        if (number < 0) {
            throw new IllegalArgumentException(
                    name + " không được âm"
            );
        }
        if ("warrantyMonths".equals(name) && number > Short.MAX_VALUE) {
            throw new IllegalArgumentException("Thời gian bảo hành vượt giới hạn cho phép");
        }

        return number;
    }

    private static int parseInt(HttpServletRequest request,
                                String name) {
        try {
            return Integer.parseInt(required(request, name));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Giá trị " + name + " không hợp lệ"
            );
        }
    }

    private static BigDecimal price(HttpServletRequest request,
                                    String name) {
        try {
            BigDecimal result =
                    new BigDecimal(required(request, name));

            if (result.signum() < 0 || result.scale() > 2
                    || result.precision() - result.scale() > 16) {
                throw new IllegalArgumentException(
                        "Giá tiền không hợp lệ"
                );
            }

            return result;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Giá tiền không hợp lệ"
            );
        }
    }
}
