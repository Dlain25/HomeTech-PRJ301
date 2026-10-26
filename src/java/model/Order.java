package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Order {

    private long orderId;
    private int userId;
    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private String note;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private String orderStatus;
    private LocalDateTime createdAt;
    private String paymentMethod;
    private String paymentStatus;
    private List<OrderItem> items = new ArrayList<>();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public String getCreatedAtText() {
        return createdAt == null ? "" : createdAt.format(FMT);
    }

    public boolean isCancellable() {
        return "PENDING".equals(orderStatus);
    }

    public String getStatusText() {
        if (orderStatus == null) {
            return "";
        }
        switch (orderStatus) {
            case "PENDING":
                return "Chờ xác nhận";
            case "CONFIRMED":
                return "Đã xác nhận";
            case "SHIPPING":
                return "Đang giao";
            case "DELIVERED":
                return "Đã giao";
            case "CANCELLED":
                return "Đã hủy";
            default:
                return orderStatus;
        }
    }

    public String getStatusBadge() {
        if (orderStatus == null) {
            return "text-bg-light";
        }
        switch (orderStatus) {
            case "PENDING":
                return "text-bg-warning";
            case "CONFIRMED":
                return "text-bg-info";
            case "SHIPPING":
                return "text-bg-primary";
            case "DELIVERED":
                return "text-bg-success";
            case "CANCELLED":
                return "text-bg-secondary";
            default:
                return "text-bg-light";
        }
    }

    public String getPaymentMethodText() {
        if (paymentMethod == null) {
            return "";
        }
        switch (paymentMethod) {
            case "COD":
                return "Thanh toán khi nhận hàng";
            case "BANK_TRANSFER":
                return "Chuyển khoản ngân hàng";
            case "VNPAY":
                return "VNPay";
            case "MOMO":
                return "MoMo";
            default:
                return paymentMethod;
        }
    }

    public String getPaymentStatusText() {
        if (paymentStatus == null) {
            return "";
        }
        switch (paymentStatus) {
            case "PENDING":
                return "Chưa thanh toán";
            case "PAID":
                return "Đã thanh toán";
            case "FAILED":
                return "Thất bại";
            case "REFUNDED":
                return "Đã hoàn tiền";
            default:
                return paymentStatus;
        }
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

}
