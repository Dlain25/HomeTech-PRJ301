
package dal;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DashboardDAO {

    // Tổng số sản phẩm
    public int getTotalProducts() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Products";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // Tổng số người dùng
    public int getTotalUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Users";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // Tổng số đơn hàng
    public int getTotalOrders() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Orders";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // Doanh thu từ đơn hàng đã giao
    public BigDecimal getTotalRevenue() throws SQLException {
        String sql =
            "SELECT COALESCE(SUM(total_amount), 0) "
            + "FROM Orders WHERE order_status = 'DELIVERED'";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getBigDecimal(1)
                             : BigDecimal.ZERO;
        }
    }

    // Doanh thu theo tháng trong năm hiện tại
    public List<Object[]> getMonthlyRevenue() throws SQLException {
        List<Object[]> list = new ArrayList<>();

        String sql =
            "SELECT MONTH(created_at) AS month_number, "
            + "SUM(total_amount) AS revenue "
            + "FROM Orders "
            + "WHERE order_status = 'DELIVERED' "
            + "AND YEAR(created_at) = YEAR(GETDATE()) "
            + "GROUP BY MONTH(created_at) "
            + "ORDER BY month_number";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("month_number"),
                    rs.getBigDecimal("revenue")
                });
            }
        }

        return list;
    }

    // Top 5 sản phẩm bán chạy
    public List<Object[]> getTopSellingProducts() throws SQLException {
        List<Object[]> list = new ArrayList<>();

        String sql =
            "SELECT TOP 5 p.product_name, "
            + "SUM(oi.quantity) AS total_sold, "
            + "SUM(oi.line_total) AS revenue "
            + "FROM OrderItems oi "
            + "JOIN Orders o ON oi.order_id = o.order_id "
            + "JOIN ProductVariants pv "
            + "ON oi.variant_id = pv.variant_id "
            + "JOIN Products p ON pv.product_id = p.product_id "
            + "WHERE o.order_status = 'DELIVERED' "
            + "GROUP BY p.product_id, p.product_name "
            + "ORDER BY total_sold DESC";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("product_name"),
                    rs.getInt("total_sold"),
                    rs.getBigDecimal("revenue")
                });
            }
        }

        return list;
    }
}
