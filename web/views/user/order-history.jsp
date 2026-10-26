
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lịch sử đơn hàng - HomeTech</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            font-family: Arial, sans-serif;
            background: #f3f6fa;
            margin: 0;
            color: #253247;
        }

        .container {
            max-width: 1150px;
            margin: 40px auto;
            padding: 0 16px;
        }

        h1 {
            color: #1769aa;
            margin-bottom: 10px;
        }

        a {
            color: #1769aa;
            text-decoration: none;
        }

        .table-box {
            background: white;
            border-radius: 12px;
            overflow-x: auto;
            margin-top: 25px;
            box-shadow: 0 3px 15px rgba(0,0,0,.06);
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        th, td {
            padding: 17px;
            text-align: left;
            border-bottom: 1px solid #e7edf3;
        }

        th {
            background: #eaf2fa;
            color: #173b66;
        }

        .price {
            font-weight: bold;
            color: #d93025;
            white-space: nowrap;
        }

        .status {
            display: inline-block;
            background: #fff1d6;
            color: #925b00;
            padding: 7px 11px;
            border-radius: 6px;
            font-size: 13px;
        }

        .status.cancelled {
            background: #fde8e8;
            color: #a32121;
        }

        .status.completed {
            background: #d4edda;
            color: #155724;
        }

        .btn {
            display: inline-block;
            background: #1769aa;
            color: white;
            padding: 10px 14px;
            border-radius: 6px;
            white-space: nowrap;
        }

        .empty {
            background: white;
            padding: 60px 20px;
            text-align: center;
            border-radius: 12px;
            margin-top: 25px;
        }

        .message {
            background: #d4edda;
            color: #155724;
            padding: 14px;
            border-radius: 8px;
            margin-top: 20px;
        }

        .message.error {
            background: #f8d7da;
            color: #721c24;
        }
    </style>
</head>

<body>

<div class="container">

    <h1>Lịch sử đơn hàng HomeTech</h1>

    <a href="${pageContext.request.contextPath}/">
        Quay về trang chủ
    </a>

    <c:if test="${not empty sessionScope.flashSuccess}">
        <div class="message">
            <c:out value="${sessionScope.flashSuccess}"/>
        </div>
        <c:remove var="flashSuccess" scope="session"/>
    </c:if>

    <c:if test="${not empty sessionScope.flashError}">
        <div class="message error">
            <c:out value="${sessionScope.flashError}"/>
        </div>
        <c:remove var="flashError" scope="session"/>
    </c:if>

    <c:choose>

        <c:when test="${empty orders}">
            <div class="empty">
                <h2>Bạn chưa có đơn hàng nào</h2>
                <p>Hãy khám phá các sản phẩm của HomeTech.</p>

                <a class="btn"
                   href="${pageContext.request.contextPath}/products">
                    Mua sắm ngay
                </a>
            </div>
        </c:when>

        <c:otherwise>

            <div class="table-box">
                <table>
                    <thead>
                        <tr>
                            <th>Mã đơn hàng</th>
                            <th>Ngày đặt</th>
                            <th>Tổng tiền</th>
                            <th>Trạng thái</th>
                            <th>Thanh toán</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>

                    <tbody>
                        <c:forEach var="order" items="${orders}">
                            <tr>
                                <td>
                                    <strong>#${order.orderId}</strong>
                                </td>

                                <td>
                                    <c:out value="${order.createdAt}"/>
                                </td>

                                <td class="price">
                                    <fmt:formatNumber
                                        value="${order.totalAmount}"
                                        pattern="#,##0"/> đ
                                </td>

                                <td>
                                    <span class="status ${order.orderStatus == 'CANCELLED' ? 'cancelled' : (order.orderStatus == 'COMPLETED' ? 'completed' : '')}">
                                        <c:out value="${order.orderStatus}"/>
                                    </span>
                                </td>

                                <td>
                                    <c:choose>
                                        <c:when test="${order.paymentMethod == 'COD'}">
                                            Thanh toán khi nhận hàng
                                        </c:when>
                                        <c:when test="${order.paymentMethod == 'BANK_TRANSFER'}">
                                            Chuyển khoản
                                        </c:when>
                                        <c:otherwise>
                                            <c:out value="${order.paymentMethod}"/>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <td>
                                    <a class="btn"
                                       href="${pageContext.request.contextPath}/orders?id=${order.orderId}">
                                        Xem chi tiết
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>

        </c:otherwise>
    </c:choose>

</div>

</body>
</html>
