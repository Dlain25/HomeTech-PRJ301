
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Giỏ hàng - HomeTech</title>
    <style>
        * { box-sizing: border-box; }

        body {
            font-family: Arial, sans-serif;
            background: #f3f6fa;
            margin: 0;
            color: #243247;
        }

        .container {
            max-width: 1100px;
            margin: 40px auto;
            padding: 0 16px;
        }

        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 15px;
        }

        h1 { color: #1769aa; }

        a { color: #1769aa; text-decoration: none; }

        .cart-table {
            width: 100%;
            border-collapse: collapse;
            background: white;
            border-radius: 10px;
            overflow: hidden;
        }

        th, td {
            padding: 15px;
            border-bottom: 1px solid #eee;
            text-align: left;
        }

        th { background: #eaf2fa; }

        .product {
            display: flex;
            align-items: center;
            gap: 15px;
        }

        .product img {
            width: 80px;
            height: 80px;
            object-fit: contain;
            border-radius: 8px;
        }

        .quantity {
            width: 65px;
            padding: 8px;
        }

        button, .checkout {
            border: none;
            padding: 10px 15px;
            border-radius: 6px;
            cursor: pointer;
            background: #1769aa;
            color: white;
        }

        .remove { background: #dc3545; }

        .summary {
            background: white;
            padding: 25px;
            margin-top: 20px;
            border-radius: 10px;
            text-align: right;
        }

        .total {
            font-size: 24px;
            font-weight: bold;
            color: #d93025;
        }

        .checkout {
            display: inline-block;
            margin-top: 15px;
        }

        .empty {
            background: white;
            padding: 50px;
            text-align: center;
            border-radius: 10px;
        }

        .message {
            padding: 12px;
            border-radius: 6px;
            margin-bottom: 15px;
        }

        .success { background: #d4edda; color: #155724; }
        .error { background: #f8d7da; color: #721c24; }

        @media (max-width: 700px) {
            .cart-table { display: block; overflow-x: auto; }
            .header { flex-direction: column; align-items: flex-start; }
        }
    </style>
</head>
<body>

<div class="container">

    <div class="header">
        <h1>Giỏ hàng HomeTech</h1>
        <a href="${pageContext.request.contextPath}/">
            Tiếp tục mua sắm
        </a>
    </div>

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

    <c:choose>
        <c:when test="${empty items}">
            <div class="empty">
                <h2>Giỏ hàng của bạn đang trống</h2>
                <p>Hãy chọn sản phẩm yêu thích để bắt đầu mua sắm.</p>
                <a href="${pageContext.request.contextPath}/">
                    Quay về trang chủ
                </a>
            </div>
        </c:when>

        <c:otherwise>
            <table class="cart-table">
                <thead>
                    <tr>
                        <th>Sản phẩm</th>
                        <th>Đơn giá</th>
                        <th>Số lượng</th>
                        <th>Thành tiền</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>

                <tbody>
                    <c:forEach var="item" items="${items}">
                        <tr>
                            <td>
                                <div class="product">
                                    <c:if test="${not empty item.imageUrl}">
                                        <img src="${pageContext.request.contextPath}/${item.imageUrl}"
                                             alt="Sản phẩm">
                                    </c:if>
                                    <div>
                                        <strong><c:out value="${item.productName}"/></strong>
                                        <p><c:out value="${item.variantName}"/></p>
                                        <small><c:out value="${item.color}"/></small>
                                    </div>
                                </div>
                            </td>

                            <td>
                                <fmt:formatNumber value="${item.unitPrice}"
                                                  pattern="#,##0"/> đ
                            </td>

                            <td>
                                <form action="${pageContext.request.contextPath}/cart"
                                      method="post">
                                    <input type="hidden" name="action" value="update">
                                    <input type="hidden" name="cartItemId"
                                           value="${item.cartItemId}">
                                    <input class="quantity" type="number"
                                           name="quantity"
                                           value="${item.quantity}"
                                           min="1"
                                           max="${item.stock}">
                                    <button type="submit">Cập nhật</button>
                                </form>
                            </td>

                            <td>
                                <fmt:formatNumber value="${item.lineTotal}"
                                                  pattern="#,##0"/> đ
                            </td>

                            <td>
                                <form action="${pageContext.request.contextPath}/cart"
                                      method="post">
                                    <input type="hidden" name="action" value="remove">
                                    <input type="hidden" name="cartItemId"
                                           value="${item.cartItemId}">
                                    <button class="remove" type="submit">
                                        Xóa
                                    </button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>

            <div class="summary">
                <h2>Tổng thanh toán</h2>
                <div class="total">
                    <fmt:formatNumber value="${total}" pattern="#,##0"/> đ
                </div>

                <a class="checkout"
                   href="${pageContext.request.contextPath}/checkout">
                    Tiến hành thanh toán
                </a>
            </div>
        </c:otherwise>
    </c:choose>

</div>
</body>
</html>
