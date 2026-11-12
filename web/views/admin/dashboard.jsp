<%@page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.text.NumberFormat"%>
<%@page import="java.util.Locale"%>

<%!
    private String esc(Object value) {
        if (value == null) {
            return "";
        }

        return String.valueOf(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String money(BigDecimal amount) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }

        NumberFormat format = NumberFormat.getNumberInstance(
                Locale.forLanguageTag("vi-VN")
        );

        format.setMaximumFractionDigits(0);
        format.setMinimumFractionDigits(0);

        return format.format(amount) + " đ";
    }
%>

<%
    String path = request.getContextPath();

    Integer totalProducts =
            (Integer) request.getAttribute("totalProducts");

    Integer totalUsers =
            (Integer) request.getAttribute("totalUsers");

    Integer totalOrders =
            (Integer) request.getAttribute("totalOrders");

    BigDecimal totalRevenue =
            (BigDecimal) request.getAttribute("totalRevenue");

    boolean canReport =
            Boolean.TRUE.equals(request.getAttribute("canReport"));

    List<Object[]> monthlyRevenue =
            (List<Object[]>) request.getAttribute("monthlyRevenue");

    List<Object[]> topSellingProducts =
            (List<Object[]>) request.getAttribute("topSellingProducts");
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>HomeTech - Quản trị</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            background: #f4f6fa;
            color: #243047;
            font-family: Arial, Helvetica, sans-serif;
            line-height: 1.5;
        }

        a {
            color: #1769aa;
        }

        a:focus-visible {
            outline: 3px solid #77b9ef;
            outline-offset: 4px;
        }

        /* Tiêu đề */
        .admin-header {
            background: #173b66;
            color: #fff;
            padding: 24px 32px;
        }

        .admin-header h1 {
            margin: 0 0 6px;
            font-size: clamp(24px, 3vw, 34px);
            line-height: 1.25;
        }

        .admin-header p {
            margin: 0;
            color: #e0ebf8;
            font-size: 16px;
        }

        /* Một thanh menu, luôn nằm trên một hàng */
        .admin-nav {
            display: flex;
            align-items: center;
            flex-wrap: nowrap;
            gap: 20px;
            padding: 0 32px;
            background: #fff;
            border-bottom: 1px solid #dce3ec;
            overflow-x: auto;
        }

        .admin-nav a {
            display: block;
            flex: 0 0 auto;
            padding: 17px 0 14px;
            border-bottom: 3px solid transparent;
            color: #173b66;
            font-size: 14px;
            font-weight: 600;
            text-decoration: none;
            white-space: nowrap;
        }

        .admin-nav a:hover,
        .admin-nav a.active {
            color: #086ac1;
            border-bottom-color: #086ac1;
        }

        /* Nội dung */
        .dashboard {
            width: 100%;
            max-width: 1400px;
            margin: 28px auto;
            padding: 0 24px 32px;
        }

        .page-heading {
            margin-bottom: 22px;
        }

        .page-heading h2 {
            margin: 0 0 5px;
            color: #173b66;
            font-size: 24px;
        }

        .page-heading p {
            margin: 0;
            color: #64748b;
        }

        /* Thẻ thống kê */
        .stats {
            display: grid;
            grid-template-columns: repeat(4, minmax(0, 1fr));
            gap: 18px;
        }

        .stat-card {
            min-width: 0;
            padding: 24px;
            background: #fff;
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            box-shadow: 0 3px 12px rgba(23, 59, 102, 0.04);
        }

        .stat-label {
            margin: 0 0 12px;
            color: #64748b;
            font-size: 14px;
            font-weight: 600;
        }

        .stat-value {
            color: #173b66;
            font-size: clamp(23px, 2.3vw, 32px);
            font-weight: 700;
            line-height: 1.3;
            overflow-wrap: anywhere;
        }

        .stat-value.restricted {
            color: #64748b;
            font-size: 17px;
            font-weight: 500;
        }

        .revenue-note {
            margin: 18px 0 0;
            color: #64748b;
            font-size: 14px;
        }

        .revenue-note a {
            font-weight: 600;
        }

        /* Bảng dữ liệu */
        .panel {
            margin-top: 26px;
            overflow: hidden;
            background: #fff;
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            box-shadow: 0 3px 12px rgba(23, 59, 102, 0.04);
        }

        .panel-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            flex-wrap: wrap;
            gap: 12px;
            padding: 20px 24px;
            border-bottom: 1px solid #e2e8f0;
        }

        .panel-header h2 {
            margin: 0;
            color: #173b66;
            font-size: 20px;
        }

        .panel-header a {
            font-size: 14px;
            font-weight: 600;
            text-decoration: none;
        }

        .panel-header a:hover {
            text-decoration: underline;
        }

        .table-wrap {
            overflow-x: auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        th,
        td {
            padding: 15px 24px;
            text-align: left;
            border-bottom: 1px solid #edf1f6;
        }

        th {
            background: #f1f5fa;
            color: #173b66;
            font-size: 14px;
            font-weight: 600;
            white-space: nowrap;
        }

        td {
            font-size: 14px;
        }

        tbody tr:last-child td {
            border-bottom: none;
        }

        tbody tr:hover {
            background: #f8fafc;
        }

        .number {
            text-align: right;
            white-space: nowrap;
            font-variant-numeric: tabular-nums;
        }

        .empty {
            padding: 32px 24px;
            color: #64748b;
            text-align: center;
        }

        .permission-message {
            margin: 0;
            padding: 28px 24px;
            color: #64748b;
        }

        @media (max-width: 1000px) {
            .stats {
                grid-template-columns: repeat(2, minmax(0, 1fr));
            }
        }

        @media (max-width: 560px) {
            .admin-header {
                padding: 22px 16px;
            }

            .admin-nav {
                gap: 18px;
                padding: 0 16px;
            }

            .dashboard {
                margin-top: 20px;
                padding: 0 16px 24px;
            }

            .stats {
                grid-template-columns: 1fr;
                gap: 12px;
            }

            .stat-card {
                padding: 20px;
            }

            .panel-header,
            th,
            td {
                padding: 14px 16px;
            }
        }
    </style>
</head>

<body>

<header class="admin-header">
    <h1>HomeTech Admin Dashboard</h1>
    <p>Thống kê hoạt động kinh doanh</p>
</header>

<nav class="admin-nav" aria-label="Menu quản trị">
    <a href="<%= esc(path) %>/admin/dashboard"
       class="active"
       aria-current="page">
        Tổng quan
    </a>

    <a href="<%= esc(path) %>/admin/categories">
        Danh mục
    </a>

    <a href="<%= esc(path) %>/admin/products">
        Sản phẩm
    </a>

    <a href="<%= esc(path) %>/admin/users">
        Người dùng
    </a>

    <a href="<%= esc(path) %>/admin/orders">
        Duyệt đơn &amp; thu tiền
    </a>

    <a href="<%= esc(path) %>/admin/reports">
        Báo cáo &amp; tiến độ
    </a>

    <a href="<%= esc(path) %>/admin/topups">
        Duyệt nạp ví
    </a>

    <a href="<%= esc(path) %>/admin/services">
        Gói dịch vụ
    </a>

    <a href="<%= esc(path) %>/admin/returns">
        Hoàn hàng
    </a>

    <a href="<%= esc(path) %>/home">
        Trang chủ
    </a>
</nav>

<main class="dashboard">

    <div class="page-heading">
        <h2>Tổng quan hệ thống</h2>
        <p>Theo dõi sản phẩm, người dùng, đơn hàng và doanh thu.</p>
    </div>

    <div class="stats">
        <div class="stat-card">
            <h3 class="stat-label">Tổng sản phẩm</h3>
            <div class="stat-value">
                <%= totalProducts == null ? "—" : totalProducts %>
            </div>
        </div>

        <div class="stat-card">
            <h3 class="stat-label">Tổng người dùng</h3>
            <div class="stat-value">
                <%= totalUsers == null ? "—" : totalUsers %>
            </div>
        </div>

        <div class="stat-card">
            <h3 class="stat-label">Tổng đơn hàng</h3>
            <div class="stat-value">
                <%= totalOrders == null ? "—" : totalOrders %>
            </div>
        </div>

        <div class="stat-card">
            <h3 class="stat-label">Tổng doanh thu thuần</h3>

            <% if (canReport) { %>
                <div class="stat-value">
                    <%= totalRevenue == null ? "—" : money(totalRevenue) %>
                </div>
            <% } else { %>
                <div class="stat-value restricted">
                    Chưa được cấp quyền xem báo cáo
                </div>
            <% } %>
        </div>
    </div>

    <% if (canReport) { %>
        <p class="revenue-note">
            Doanh thu thuần gồm các đơn đã giao và thu đủ tiền,
            trừ khoản hoàn tiền theo ngày hoàn.
            Đơn cũ thiếu ngày giao chưa được tính.
            <a href="<%= esc(path) %>/admin/reports">
                Xem báo cáo chi tiết
            </a>
        </p>
    <% } %>

    <section class="panel" aria-labelledby="monthly-title">
        <div class="panel-header">
            <h2 id="monthly-title">Doanh thu theo tháng</h2>

            <% if (canReport) { %>
                <a href="<%= esc(path) %>/admin/reports">
                    Xem báo cáo
                </a>
            <% } %>
        </div>

        <% if (!canReport) { %>

            <p class="permission-message">
                Tài khoản chưa được cấp quyền xem dữ liệu doanh thu.
            </p>

        <% } else { %>

            <div class="table-wrap">
                <table aria-labelledby="monthly-title">
                    <thead>
                        <tr>
                            <th scope="col">Tháng</th>
                            <th scope="col" class="number">Doanh thu thuần</th>
                        </tr>
                    </thead>

                    <tbody>
                        <%
                            if (monthlyRevenue != null
                                    && !monthlyRevenue.isEmpty()) {

                                for (Object[] row : monthlyRevenue) {
                        %>
                            <tr>
                                <td>Tháng <%= esc(row[0]) %></td>
                                <td class="number">
                                    <%= money((BigDecimal) row[1]) %>
                                </td>
                            </tr>
                        <%
                                }
                            } else {
                        %>
                            <tr>
                                <td colspan="2" class="empty">
                                    Chưa có dữ liệu doanh thu.
                                </td>
                            </tr>
                        <%
                            }
                        %>
                    </tbody>
                </table>
            </div>

        <% } %>
    </section>

    <section class="panel" aria-labelledby="products-title">
        <div class="panel-header">
            <h2 id="products-title">Top 5 sản phẩm bán chạy</h2>

            <a href="<%= esc(path) %>/admin/products">
                Quản lý sản phẩm
            </a>
        </div>

        <% if (!canReport) { %>

            <p class="permission-message">
                Tài khoản chưa được cấp quyền xem thống kê bán hàng.
            </p>

        <% } else { %>

            <div class="table-wrap">
                <table aria-labelledby="products-title">
                    <thead>
                        <tr>
                            <th scope="col">Sản phẩm</th>
                            <th scope="col" class="number">Số lượng đã bán</th>
                            <th scope="col" class="number">Doanh thu</th>
                        </tr>
                    </thead>

                    <tbody>
                        <%
                            if (topSellingProducts != null
                                    && !topSellingProducts.isEmpty()) {

                                for (Object[] row : topSellingProducts) {
                        %>
                            <tr>
                                <td><%= esc(row[0]) %></td>
                                <td class="number"><%= esc(row[1]) %></td>
                                <td class="number">
                                    <%= money((BigDecimal) row[2]) %>
                                </td>
                            </tr>
                        <%
                                }
                            } else {
                        %>
                            <tr>
                                <td colspan="3" class="empty">
                                    Chưa có sản phẩm bán chạy.
                                </td>
                            </tr>
                        <%
                            }
                        %>
                    </tbody>
                </table>
            </div>

        <% } %>
    </section>

</main>

</body>
</html>