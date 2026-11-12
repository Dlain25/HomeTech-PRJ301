package dal;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;

/** Separate payment confirmation from order approval. Own connection per DAO. */
public final class PaymentReviewDAO extends BaseDAO {
    public PaymentReviewDAO() throws SQLException { super(); }

    public static void requirePermission(Connection con, int actorId, String permission) throws SQLException {
        String sql = "SELECT 1 FROM dbo.Users u JOIN dbo.Roles r ON r.role_id=u.role_id "
                + "JOIN dbo.AdminPermissions a ON a.user_id=u.user_id "
                + "WHERE u.user_id=? AND u.status='ACTIVE' AND r.role_name='ADMIN' AND a.permission_code=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, actorId); ps.setString(2, permission);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Admin permission required", "HT403");
            }
        }
    }

    /**
     * actorId MUST come from verified session, never a request parameter.
     * receivedAtUtc is the UTC timestamp from the verified bank statement.
     * Returns false when already confirmed; never adds a second receipt.
     */
    public boolean confirmBankPayment(long orderId, int actorId, String bankReference,
            BigDecimal receivedAmount, LocalDateTime receivedAtUtc) throws SQLException {
        if (bankReference == null || bankReference.isBlank() || bankReference.length() > 150
                || receivedAmount == null || receivedAmount.signum() <= 0 || receivedAtUtc == null)
            throw new IllegalArgumentException("Thông tin giao dịch ngân hàng không hợp lệ");
        if (!connection.getAutoCommit()) throw new SQLException("Use a dedicated DAO connection");
        connection.setAutoCommit(false);
        try {
            requirePermission(connection, actorId, "PAYMENT_REVIEW");
            BigDecimal total;
            String status;
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT total_amount, order_status FROM dbo.Orders WITH (UPDLOCK,HOLDLOCK) WHERE order_id=?")) {
                ps.setLong(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Order not found", "HT404");
                    total=rs.getBigDecimal(1); status=rs.getString(2);
                }
            }
            if ("CANCELLED".equals(status)) throw new SQLException("Order cancelled", "HT409");
            long paymentId;
            String paymentStatus;
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT payment_id,payment_method,payment_status,amount FROM dbo.Payments "
                    + "WITH (UPDLOCK,HOLDLOCK) WHERE order_id=?")) {
                ps.setLong(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Missing payment", "HT409");
                    paymentId=rs.getLong(1); paymentStatus=rs.getString(3);
                    if (!"BANK_TRANSFER".equals(rs.getString(2)) || total.compareTo(rs.getBigDecimal(4)) != 0)
                        throw new SQLException("Inconsistent payment", "HT409");
                    if (rs.next()) throw new SQLException("Multiple payments require manual reconciliation", "HT409");
                }
            }
            if ("PAID".equals(paymentStatus)) { connection.commit(); return false; }
            if (!"PENDING".equals(paymentStatus) || total.compareTo(receivedAmount) != 0)
                throw new SQLException("Wrong payment state or amount", "HT409");
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT dbo.BankReceipts(bank_reference,payment_id,amount,received_at,recorded_by) VALUES(?,?,?,?,?)")) {
                ps.setString(1, bankReference.trim()); ps.setLong(2,paymentId);
                ps.setBigDecimal(3,receivedAmount); ps.setTimestamp(4,Timestamp.valueOf(receivedAtUtc));
                ps.setInt(5,actorId); ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE dbo.Payments SET payment_status='PAID', transaction_reference=?, paid_at=?, "
                    + "confirmed_by=?, confirmed_at=SYSUTCDATETIME() WHERE payment_id=? AND payment_status='PENDING'")) {
                ps.setString(1,bankReference.trim()); ps.setTimestamp(2,Timestamp.valueOf(receivedAtUtc));
                ps.setInt(3,actorId); ps.setLong(4,paymentId);
                if (ps.executeUpdate()!=1) throw new SQLException("Payment changed", "HT409");
            }
            connection.commit();
            return true;
        } catch (SQLException | RuntimeException e) {
            try { connection.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
            throw e;
        } finally { connection.setAutoCommit(true); }
    }
}
