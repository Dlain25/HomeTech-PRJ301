package dal;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import model.Order;
import model.OrderItem;

public class OrderDAO extends BaseDAO {
    public OrderDAO() throws SQLException {
    super();
}
    // Phí vận chuyển cố định. Nhóm có thể đổi con số này.
    public static final BigDecimal SHIPPING_FEE = BigDecimal.ZERO;

    // Lỗi nghiệp vụ (giỏ trống, hết hàng...) để hiện cho người dùng
    public static class OrderException extends Exception {

        public OrderException(String message) {
            super(message);
        }
    }

    private void rollbackQuietly() {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }

    public long createOrder(int userId, String recipientName, String recipientPhone,
            String address, String note, String paymentMethod) throws OrderException {
        return createOrder(userId,recipientName,recipientPhone,address,note,paymentMethod,
            java.time.LocalDate.now(util.DeliveryDateUtil.ZONE).plusDays(1),"ANY");
    }

    public long createOrder(int userId, String recipientName, String recipientPhone,
            String address, String note, String paymentMethod, java.time.LocalDate deliveryDate, String deliverySlot) throws OrderException {
        try { util.DeliveryDateUtil.parse(deliveryDate.toString(),deliverySlot,java.time.Clock.systemUTC()); }
        catch (RuntimeException e) { throw new OrderException("Ngày nhận hàng không hợp lệ"); }
        if (!"COD".equals(paymentMethod) && !"BANK_TRANSFER".equals(paymentMethod)) throw new OrderException("Phương thức thanh toán không hợp lệ");
        boolean oldAuto = true;
        try {
            oldAuto = connection.getAutoCommit();
            connection.setAutoCommit(false);

            // 1. Đọc giỏ hàng, lấy GIÁ từ database (không tin giá do trình duyệt gửi)
            List<OrderItem> items = new ArrayList<>();
            String sqlItems = "SELECT ci.variant_id, ci.quantity, v.price, v.variant_sku, v.is_active, "
                    + "p.product_name, p.status "
                    + "FROM Carts c WITH (UPDLOCK, HOLDLOCK) JOIN CartItems ci WITH (UPDLOCK, HOLDLOCK) ON ci.cart_id = c.cart_id "
                    + "JOIN ProductVariants v ON v.variant_id = ci.variant_id "
                    + "JOIN Products p ON p.product_id = v.product_id "
                    + "WHERE c.user_id = ? ORDER BY ci.variant_id";
            try (PreparedStatement ps = connection.prepareStatement(sqlItems)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String name = rs.getString("product_name");
                        if (!rs.getBoolean("is_active") || !"ACTIVE".equals(rs.getString("status"))) {
                            throw new OrderException("Sản phẩm \"" + name + "\" hiện không được bán, hãy xóa khỏi giỏ");
                        }
                        OrderItem it = new OrderItem();
                        it.setVariantId(rs.getInt("variant_id"));
                        it.setQuantity(rs.getInt("quantity"));
                        it.setUnitPrice(rs.getBigDecimal("price"));
                        it.setVariantSku(rs.getString("variant_sku"));
                        it.setProductName(name);
                        items.add(it);
                    }
                }
            }
            if (items.isEmpty()) {
                throw new OrderException("Giỏ hàng đang trống");
            }

            BigDecimal subtotal = BigDecimal.ZERO;
            for (OrderItem it : items) {
                subtotal = subtotal.add(it.getLineTotal());
            }

            if ("BANK_TRANSFER".equals(paymentMethod)) {
                try { util.VietQrUtil.fromEnvironment(subtotal.add(SHIPPING_FEE), "HTCHECK"); }
                catch (RuntimeException e) { throw new OrderException("Chuyển khoản chưa được cấu hình hoặc số tiền QR không hợp lệ. Vui lòng chọn COD."); }
            }
            // 2. Trừ tồn kho (chỉ trừ được khi còn đủ hàng)
            String sqlStock = "UPDATE Inventory SET quantity_available = quantity_available - ?, "
                    + "updated_at = SYSUTCDATETIME() WHERE variant_id = ? AND quantity_available >= ?";
            try (PreparedStatement ps = connection.prepareStatement(sqlStock)) {
                for (OrderItem it : items) {
                    ps.setInt(1, it.getQuantity());
                    ps.setInt(2, it.getVariantId());
                    ps.setInt(3, it.getQuantity());
                    if (ps.executeUpdate() == 0) {
                        throw new OrderException("Sản phẩm \"" + it.getProductName() + "\" không đủ hàng");
                    }
                }
            }

            // 3. Tạo đơn hàng (total_amount là cột tự tính, không đưa vào INSERT)
            long orderId;
            String sqlOrder = "INSERT INTO Orders (user_id, recipient_name, recipient_phone, "
                    + "shipping_address, note, subtotal, shipping_fee, desired_delivery_date, delivery_slot) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setString(2, recipientName);
                ps.setString(3, recipientPhone);
                ps.setString(4, address);
                if (note == null || note.isEmpty()) {
                    ps.setNull(5, Types.NVARCHAR);
                } else {
                    ps.setString(5, note);
                }
                ps.setBigDecimal(6, subtotal);
                ps.setBigDecimal(7, SHIPPING_FEE);
                ps.setDate(8,java.sql.Date.valueOf(deliveryDate));
                ps.setString(9,deliverySlot);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Không lấy được mã đơn hàng");
                    }
                    orderId = keys.getLong(1);
                }
            }

            // 4. Chi tiết đơn (lưu lại tên, SKU, giá tại thời điểm mua; line_total tự tính)
            String sqlItem = "INSERT INTO OrderItems (order_id, variant_id, product_name, variant_sku, "
                    + "quantity, unit_price) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sqlItem)) {
                for (OrderItem it : items) {
                    ps.setLong(1, orderId);
                    ps.setInt(2, it.getVariantId());
                    ps.setString(3, it.getProductName());
                    ps.setString(4, it.getVariantSku());
                    ps.setInt(5, it.getQuantity());
                    ps.setBigDecimal(6, it.getUnitPrice());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // 5. Thanh toán (trạng thái mặc định PENDING)
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO Payments (order_id, payment_method, amount, transfer_code) VALUES (?, ?, ?, ?)")) {
                ps.setLong(1, orderId);
                ps.setString(2, paymentMethod);
                ps.setBigDecimal(3, subtotal.add(SHIPPING_FEE));
                ps.setString(4,"BANK_TRANSFER".equals(paymentMethod)?"HTOD"+orderId:null);
                ps.executeUpdate();
            }

            // 6. Xóa giỏ hàng
            try (PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM CartItems WHERE cart_id IN (SELECT cart_id FROM Carts WHERE user_id = ?)")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE Carts SET updated_at = SYSUTCDATETIME() WHERE user_id = ?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            connection.commit();
            return orderId;
        } catch (OrderException e) {
            rollbackQuietly();
            throw e;
        } catch (SQLException e) {
            rollbackQuietly();
            e.printStackTrace();
            throw new OrderException("Không thể đặt hàng lúc này, vui lòng thử lại");
        } finally {
            try {
                connection.setAutoCommit(oldAuto);
            } catch (SQLException ignored) {
            }
        }
    }

    //lịch sử đơn hàng
    private static final String ORDER_SELECT
            = "SELECT o.order_id, o.user_id, o.recipient_name, o.recipient_phone, o.shipping_address, o.note, "
            + "o.subtotal, o.shipping_fee, o.total_amount, o.order_status, o.created_at, o.desired_delivery_date, o.delivery_slot, o.delivered_at, "
            + "(SELECT TOP 1 pay.payment_method FROM Payments pay WHERE pay.order_id = o.order_id "
            + " ORDER BY pay.payment_id DESC) AS payment_method, "
            + "(SELECT TOP 1 pay.payment_status FROM Payments pay WHERE pay.order_id = o.order_id "
            + " ORDER BY pay.payment_id DESC) AS payment_status "
            + "FROM Orders o ";

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getLong("order_id"));
        java.sql.Date delivery=rs.getDate("desired_delivery_date");
        o.setDesiredDeliveryDate(delivery==null?null:delivery.toLocalDate());
        o.setDeliverySlot(rs.getString("delivery_slot"));
        java.sql.Timestamp delivered=rs.getTimestamp("delivered_at");
        o.setDeliveredAt(delivered==null?null:delivered.toLocalDateTime().plusHours(7));
        o.setUserId(rs.getInt("user_id"));
        o.setRecipientName(rs.getString("recipient_name"));
        o.setRecipientPhone(rs.getString("recipient_phone"));
        o.setShippingAddress(rs.getString("shipping_address"));
        o.setNote(rs.getString("note"));
        o.setSubtotal(rs.getBigDecimal("subtotal"));
        o.setShippingFee(rs.getBigDecimal("shipping_fee"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setOrderStatus(rs.getString("order_status"));
        Timestamp t = rs.getTimestamp("created_at");
        if (t != null) {
            // Database lưu giờ UTC, hiển thị theo giờ Việt Nam (UTC+7)
            o.setCreatedAt(t.toLocalDateTime().plusHours(7));
        }
        o.setPaymentMethod(rs.getString("payment_method"));
        o.setPaymentStatus(rs.getString("payment_status"));
        return o;
    }

    public List<Order> findByUser(int userId) {
        List<Order> list = new ArrayList<>();
        String sql = ORDER_SELECT + "WHERE o.user_id = ? ORDER BY o.created_at DESC, o.order_id DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapOrder(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Order findByIdAndUser(long orderId, int userId) {
        Order order = null;
        String sql = ORDER_SELECT + "WHERE o.order_id = ? AND o.user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    order = mapOrder(rs);
                }
            }
            if (order != null) {
                String sqlItems = "SELECT order_item_id, order_id, variant_id, product_name, variant_sku, "
                        + "quantity, unit_price FROM OrderItems WHERE order_id = ? ORDER BY order_item_id";
                try (PreparedStatement ps2 = connection.prepareStatement(sqlItems)) {
                    ps2.setLong(1, orderId);
                    try (ResultSet rs2 = ps2.executeQuery()) {
                        while (rs2.next()) {
                            OrderItem it = new OrderItem();
                            it.setOrderItemId(rs2.getLong("order_item_id"));
                            it.setOrderId(rs2.getLong("order_id"));
                            it.setVariantId(rs2.getInt("variant_id"));
                            it.setProductName(rs2.getString("product_name"));
                            it.setVariantSku(rs2.getString("variant_sku"));
                            it.setQuantity(rs2.getInt("quantity"));
                            it.setUnitPrice(rs2.getBigDecimal("unit_price"));
                            order.getItems().add(it);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return order;
    }

    public void cancelOrder(long orderId, int userId) throws OrderException {
        try (WorkflowDAO dao=new WorkflowDAO()) {
            dao.orderAction(userId,orderId,"cancelUser",null);
        } catch (SQLException | IllegalArgumentException e) {
            throw new OrderException(e instanceof SQLException && (((SQLException)e).getSQLState()==null || !((SQLException)e).getSQLState().startsWith("HT"))
                ? "Không thể hủy đơn lúc này, vui lòng thử lại" : e.getMessage());
        }
    }
}
