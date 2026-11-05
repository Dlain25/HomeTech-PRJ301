
<%@page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.text.NumberFormat"%>
<%@page import="java.util.Locale"%>

<%!
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String money(BigDecimal amount) {
        if (amount == null) amount = BigDecimal.ZERO;

        NumberFormat format =
                NumberFormat.getNumberInstance(
                        Locale.forLanguageTag("vi-VN"));

        format.setMaximumFractionDigits(0);

        return format.format(amount) + " đ";
    }
%>

<%
    Integer totalProducts =
            (Integer) request.getAttribute("totalProducts");

    Integer totalUsers =
            (Integer) request.getAttribute("totalUsers");

    Integer totalOrders =
            (Integer) request.getAttribute("totalOrders");

    BigDecimal totalRevenue =
            (BigDecimal) request.getAttribute("totalRevenue");

    List<Object[]> monthlyRevenue =
            (List<Object[]>) request.getAttribute("monthlyRevenue");

    List<Object[]> topSellingProducts =
            (List<Object[]>) request.getAttribute("topSellingProducts");

    String path = request.getContextPath();
%>

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>HomeTech - Admin Dashboard</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f4f6fa;
            color: #243047;
        }

        .header {
            background: #173b66;
            color: white;
            padding: 22px 35px;
        }

        .header h1 {
            margin: 0 0 8px;
        }

        .nav {
            background: white;
            padding: 15px 35px;
            border-bottom: 1px solid #ddd;

            display: flex;
            align-items: center;
            gap: 25px;
            flex-wrap: wrap;
        }

        .nav a {
            color: #173b66;
            text-decoration: none;
            font-weight: bold;
            transition: color 0.2s;
        }

        .nav a:hover {
            color: #1769aa;
        }

        .nav a.active {
            color: #1769aa;
            border-bottom: 2px solid #1769aa;
            padding-bottom: 5px;
        }

        .container {
            max-width: 1250px;
            margin: 30px auto;
            padding: 0 20px;
        }

        .stats {
            display: grid;
            grid-template-columns:
                repeat(auto-fit, minmax(220px, 1fr));
            gap: 20px;
        }

        .stat-card {
            background: white;
            border-radius: 12px;
            padding: 25px;
            box-shadow: 0 3px 12px rgba(0,0,0,.06);
        }

        .stat-card h3 {
            color: #64748b;
            font-size: 15px;
            margin: 0 0 15px;
        }

        .stat-card .value {
            color: #173b66;
            font-size: 29px;
            font-weight: bold;
        }

        .section {
            background: white;
            margin-top: 28px;
            padding: 25px;
            border-radius: 12px;
            box-shadow: 0 3px 12px rgba(0,0,0,.06);
        }

        .section h2 {
            margin-top: 0;
            color: #173b66;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        th, td {
            padding: 13px;
            text-align: left;
            border-bottom: 1px solid #e5e7eb;
        }

        th {
            background: #f0f4fa;
            color: #173b66;
        }

        .empty {
            text-align: center;
            color: #64748b;
            padding: 25px;
        }

        .table-wrap {
            overflow-x: auto;
        }
    </style>
</head>

<body>

<!-- ADMIN HEADER -->

<header class="header">

    <h1>HomeTech Admin Dashboard</h1>

    <div>
        Thống kê hoạt động kinh doanh
    </div>

</header>

<!-- ADMIN NAVIGATION -->

<nav class="nav">

    <a href="<%= path %>/admin/dashboard"
       class="active">
        Dashboard
    </a>

    <a href="<%= path %>/admin/categories">
        Categories
    </a>

    <!-- NEW: PRODUCT MANAGEMENT -->
    <a href="<%= path %>/admin/products">
        Products
    </a>

    <a href="<%= path %>/admin/users">
        Users
    </a>

    <a href="<%= path %>/home">
        Trang chủ
    </a>

</nav>

<!-- DASHBOARD CONTENT -->

<main class="container">

    <!-- STATISTICS -->

    <div class="stats">

        <div class="stat-card">

            <h3>Tổng sản phẩm</h3>

            <div class="value">
                <%= totalProducts == null ? 0 : totalProducts %>
            </div>

        </div>

        <div class="stat-card">

            <h3>Tổng người dùng</h3>

            <div class="value">
                <%= totalUsers == null ? 0 : totalUsers %>
            </div>

        </div>

        <div class="stat-card">

            <h3>Tổng đơn hàng</h3>

            <div class="value">
                <%= totalOrders == null ? 0 : totalOrders %>
            </div>

        </div>

        <div class="stat-card">

            <h3>Tổng doanh thu</h3>

            <div class="value">
                <%= money(totalRevenue) %>
            </div>

        </div>

    </div>

    <!-- MONTHLY REVENUE -->

    <section class="section">

        <h2>Doanh thu theo tháng</h2>

        <div class="table-wrap">

            <table>

                <thead>
                    <tr>
                        <th>Tháng</th>
                        <th>Doanh thu</th>
                    </tr>
                </thead>

                <tbody>

                <%
                    if (monthlyRevenue != null
                            && !monthlyRevenue.isEmpty()) {

                        for (Object[] row : monthlyRevenue) {
                %>

                    <tr>
                        <td>
                            Tháng <%= row[0] %>
                        </td>

                        <td>
                            <%= money((BigDecimal) row[1]) %>
                        </td>
                    </tr>

                <%
                        }

                    } else {
                %>

                    <tr>
                        <td colspan="2" class="empty">
                            Chưa có dữ liệu doanh thu
                        </td>
                    </tr>

                <%
                    }
                %>

                </tbody>

            </table>

        </div>

    </section>

    <!-- TOP SELLING PRODUCTS -->

    <section class="section">

        <h2>Top 5 sản phẩm bán chạy</h2>

        <div class="table-wrap">

            <table>

                <thead>
                    <tr>
                        <th>Sản phẩm</th>
                        <th>Số lượng đã bán</th>
                        <th>Doanh thu</th>
                    </tr>
                </thead>

                <tbody>

                <%
                    if (topSellingProducts != null
                            && !topSellingProducts.isEmpty()) {

                        for (Object[] row : topSellingProducts) {
                %>

                    <tr>

                        <td>
                            <%= esc((String) row[0]) %>
                        </td>

                        <td>
                            <%= row[1] %>
                        </td>

                        <td>
                            <%= money((BigDecimal) row[2]) %>
                        </td>

                    </tr>

                <%
                        }

                    } else {
                %>

                    <tr>
                        <td colspan="3" class="empty">
                            Chưa có sản phẩm bán chạy
                        </td>
                    </tr>

                <%
                    }
                %>

                </tbody>

            </table>

        </div>

    </section>

</main>

</body>
</html>
