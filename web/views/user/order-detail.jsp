
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết đơn hàng - HomeTech</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            background: #f3f6fa;
            font-family: Arial, sans-serif;
            color: #253247;
        }

        .container {
            max-width: 1100px;
            margin: 40px auto;
            padding: 0 16px;
        }

        h1 {
            color: #1769aa;
            margin-bottom: 12px;
        }

        h2 {
            color: #173b66;
            font-size: 21px;
            margin-top: 0;
        }

        a {
            color: #1769aa;
            text-decoration: none;
        }

        .box {
            background: white;
            padding: 25px;
            border-radius: 12px;
            margin-top: 22px;
            box-shadow: 0 3px 15px rgba(0,0,0,.05);
        }

        .grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 22px;
        }

        .info-row {
            margin: 13px 0;
            line-height: 1.5;
        }

        .info-label {
            color: #667085;
            display: block;
            margin-bottom: 5px;
        }

        .status {
            display: inline-block;
            padding: 8px 13px;
            border-radius: 7px;
            background: #fff0d4;
            color: #946000;
            font-weight: bold;
        }

        .status.cancelled {
            background: #fde8e8;
            color: #a32121;
        }

        .status.completed {
            background: #d4edda;
            color: #155724;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        th, td {
            padding: 15px;
            border-bottom: 1px solid #e8edf3;
            text-align: left;
        }

        th {
            background: #eaf2fa;
            color: #173b66;
        }

        .table-wrap {
            overflow-x: auto;
        }

        .total-row {
            display: flex;
            justify-content: space-between;
            margin: 15px 0;
        }

        .grand-total {
            padding-top: 18px;
            border-top: 1px solid #ddd;
            font-size: 23px;
            font-weight: bold;
            color: #d93025;
        }

        .btn {
            display: inline-block;
            background: #1769aa;
            color: white;
            padding: 12px 18px;
            border: none;
            border-radius: 7px;
            cursor: pointer;
            font-size: 15px;
        }

        .btn-danger {
            background: #dc3545;
        }

        .message {
            padding: 14px;
            border-radius: 7px;
            margin-top: 18px;
        }

        .success {
            background: #d4edda;
            color: #155724;
        }

        .error {
            background: #f8d7da;
            color: #721c24;
        }

        @media (max-width: 750px) {
            .grid {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>

<body>
<div class="container">

    <h1>Chi tiết đơn hàng #${order.orderId}</h1>

    <a href="${pageContext.request.contextPath}/orders">
        Quay lại lịch sử đơn hàng
    </a>

    <c:if test="${not empty sessionScope.flashSuccess}">
        <div class="message success">
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

    <div class="grid">

        <section class="box">
            <h2>Thông tin đơn hàng</h2>

            <div class="info-row">
                <span class="info-label">Mã đơn hàng</span>
                <strong>#${order.orderId}</strong>
            </div>

            <div class="info-row">
                <span class="info-label">Ngày đặt hàng</span>
                <c:out value="${order.createdAt}"/>
            </div>

            <div class="info-row">
                <span class="info-label">Trạng thái đơn hàng</span>
                <span class="status ${order.orderStatus == 'CANCELLED' ? 'cancelled' : (order.orderStatus == 'COMPLETED' ? 'completed' : '')}">
                    <c:out value="${order.orderStatus}"/>
                </span>
            </div>

            <div class="info-row">
                <span class="info-label">Phương thức thanh toán</span>

                <c:choose>
                    <c:when test="${order.paymentMethod == 'COD'}">
                        Thanh toán khi nhận hàng (COD)
                    </c:when>
                    <c:when test="${order.paymentMethod == 'BANK_TRANSFER'}">
                        Chuyển khoản ngân hàng
                    </c:when>
                    <c:otherwise>
                        <c:out value="${order.paymentMethod}"/>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="info-row">
                <span class="info-label">Trạng thái thanh toán</span>
                <c:out value="${order.paymentStatus}"/>
            </div>
        </section>

        <section class="box">
            <h2>Thông tin người nhận</h2>

            <div class="info-row">
                <span class="info-label">Họ và tên</span>
                <c:out value="${order.recipientName}"/>
            </div>

            <div class="info-row">
                <span class="info-label">Số điện thoại</span>
                <c:out value="${order.recipientPhone}"/>
            </div>

            <div class="info-row">
                <span class="info-label">Địa chỉ nhận hàng</span>
                <c:out value="${order.shippingAddress}"/>
            </div>

            <c:if test="${not empty order.note}">
                <div class="info-row">
                    <span class="info-label">Ghi chú</span>
                    <c:out value="${order.note}"/>
                </div>
            </c:if>
        </section>

    </div>

    <section class="box">
        <h2>Sản phẩm đã đặt</h2>

        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <th>Sản phẩm</th>
                        <th>SKU</th>
                        <th>Đơn giá</th>
                        <th>Số lượng</th>
                        <th>Thành tiền</th>
                    </tr>
                </thead>

                <tbody>
                    <c:forEach var="item" items="${order.items}">
                        <tr>
                            <td>
                                <strong>
                                    <c:out value="${item.productName}"/>
                                </strong>
                            </td>

                            <td>
                                <c:out value="${item.variantSku}"/>
                            </td>

                            <td>
                                <fmt:formatNumber
                                    value="${item.unitPrice}"
                                    pattern="#,##0"/> đ
                            </td>

                            <td>
                                ${item.quantity}
                            </td>

                            <td>
                                <strong>
                                    <fmt:formatNumber
                                        value="${item.lineTotal}"
                                        pattern="#,##0"/> đ
                                </strong>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </section>

    <section class="box">
        <h2>Tổng thanh toán</h2>

        <div class="total-row">
            <span>Tạm tính</span>
            <strong>
                <fmt:formatNumber
                    value="${order.subtotal}"
                    pattern="#,##0"/> đ
            </strong>
        </div>

        <div class="total-row">
            <span>Phí vận chuyển</span>
            <strong>
                <fmt:formatNumber
                    value="${order.shippingFee}"
                    pattern="#,##0"/> đ
            </strong>
        </div>

        <div class="total-row grand-total">
            <span>Tổng cộng</span>
            <span>
                <fmt:formatNumber
                    value="${order.totalAmount}"
                    pattern="#,##0"/> đ
            </span>
        </div>

        <c:if test="${order.orderStatus == 'PENDING'}">
            <form action="${pageContext.request.contextPath}/orders"
                  method="post"
                  onsubmit="return confirm('Bạn chắc chắn muốn hủy đơn hàng này?');">

                <input type="hidden" name="action" value="cancel">

                <input type="hidden"
                       name="orderId"
                       value="${order.orderId}">

                <button type="submit" class="btn btn-danger">
                    Hủy đơn hàng
                </button>
            </form>
        </c:if>

    </section>

</div>
</body>
</html>
