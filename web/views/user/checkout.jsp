
<%@page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thanh toán - HomeTech</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f3f6fa;
            color: #253247;
        }

        .container {
            max-width: 1150px;
            margin: 35px auto;
            padding: 0 16px;
        }

        h1 {
            color: #1769aa;
            margin-bottom: 8px;
        }

        h2 {
            font-size: 20px;
            margin-top: 0;
            color: #173b66;
        }

        a {
            color: #1769aa;
            text-decoration: none;
        }

        .layout {
            display: grid;
            grid-template-columns: 1.4fr 1fr;
            gap: 25px;
            align-items: start;
            margin-top: 25px;
        }

        .box {
            background: white;
            padding: 25px;
            border-radius: 12px;
            box-shadow: 0 3px 15px rgba(0,0,0,.06);
        }

        label {
            display: block;
            font-weight: bold;
            margin: 17px 0 7px;
        }

        input, textarea {
            width: 100%;
            padding: 12px;
            border: 1px solid #ccd5df;
            border-radius: 7px;
            font-size: 14px;
            font-family: inherit;
        }

        textarea {
            min-height: 90px;
            resize: vertical;
        }

        input:focus, textarea:focus {
            outline: none;
            border-color: #1769aa;
        }

        .error {
            color: #d93025;
            font-size: 13px;
            margin-top: 5px;
        }

        .general-error {
            background: #fde8e8;
            color: #a32121;
            padding: 13px;
            border-radius: 7px;
            margin-bottom: 15px;
        }

        .payment-option {
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 13px;
            margin-top: 12px;
            border: 1px solid #d7e0ea;
            border-radius: 8px;
            cursor: pointer;
        }

        .payment-option input {
            width: auto;
            margin: 0;
        }

        .payment-option span {
            font-weight: normal;
        }

        .order-item {
            display: flex;
            justify-content: space-between;
            gap: 15px;
            padding: 15px 0;
            border-bottom: 1px solid #edf0f4;
        }

        .product-name {
            font-weight: bold;
            margin-bottom: 6px;
        }

        .detail {
            color: #697586;
            font-size: 13px;
            margin-top: 5px;
        }

        .money {
            white-space: nowrap;
            font-weight: bold;
        }

        .total-row {
            display: flex;
            justify-content: space-between;
            gap: 15px;
            margin-top: 18px;
        }

        .grand-total {
            border-top: 1px solid #ddd;
            padding-top: 18px;
            font-size: 20px;
            font-weight: bold;
            color: #d93025;
        }

        .submit-btn {
            width: 100%;
            margin-top: 25px;
            padding: 15px;
            border: 0;
            border-radius: 8px;
            background: #1769aa;
            color: white;
            font-weight: bold;
            font-size: 16px;
            cursor: pointer;
        }

        .submit-btn:hover {
            background: #104d80;
        }

        .notice {
            color: #667085;
            font-size: 13px;
            line-height: 1.6;
        }

        @media (max-width: 800px) {
            .layout {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>

<body>
<div class="container">

    <h1>Thanh toán HomeTech</h1>

    <p>
        <a href="${pageContext.request.contextPath}/cart">
            Quay lại giỏ hàng
        </a>
    </p>

    <c:if test="${not empty errors.general}">
        <div class="general-error">
            <c:out value="${errors.general}"/>
        </div>
    </c:if>

    <form action="${pageContext.request.contextPath}/checkout"
          method="post" accept-charset="UTF-8">

        <div class="layout">

            <!-- THÔNG TIN GIAO HÀNG -->
            <section class="box">

                <h2>Thông tin người nhận</h2>

                <label for="recipientName">Họ và tên *</label>
                <input type="text"
                       id="recipientName"
                       name="recipientName"
                       value="<c:out value='${recipientName}'/>"
                       maxlength="120"
                       required>
                <div class="error">
                    <c:out value="${errors.recipientName}"/>
                </div>

                <label for="recipientPhone">Số điện thoại *</label>
                <input type="tel"
                       id="recipientPhone"
                       name="recipientPhone"
                       value="<c:out value='${recipientPhone}'/>"
                       maxlength="20"
                       required>
                <div class="error">
                    <c:out value="${errors.recipientPhone}"/>
                </div>

                <label for="shippingAddress">
                    Địa chỉ nhận hàng *
                </label>
                <textarea id="shippingAddress"
                          name="shippingAddress"
                          maxlength="500"
                          required><c:out value="${shippingAddress}"/></textarea>
                <div class="error">
                    <c:out value="${errors.shippingAddress}"/>
                </div>

                <label for="desiredDeliveryDate">Ngày có thể nhận hàng *</label>
                <input type="date" id="desiredDeliveryDate" name="desiredDeliveryDate" required
                       min="${minDeliveryDate}" max="${maxDeliveryDate}" value="<c:out value='${desiredDeliveryDate}'/>">
                <div style="display:flex;gap:8px;flex-wrap:wrap;margin-top:10px">
                  <c:forEach var="date" items="${suggestedDates}"><button type="button" data-delivery-date="${date}">${date}</button></c:forEach>
                </div>
                <label for="deliverySlot">Khung giờ có thể nhận</label>
                <select name="deliverySlot" id="deliverySlot" style="padding:12px;width:100%">
                  <option value="ANY" ${deliverySlot=='ANY'?'selected':''}>Cả ngày</option>
                  <option value="AM" ${deliverySlot=='AM'?'selected':''}>Buổi sáng</option>
                  <option value="PM" ${deliverySlot=='PM'?'selected':''}>Buổi chiều</option>
                </select>
                <div class="error"><c:out value="${errors.deliveryDate}"/></div>
                <p class="notice">Chọn từ ngày mai đến 14 ngày tới. Cửa hàng sẽ đối chiếu lịch giao khi duyệt đơn.</p>
                <label for="note">Ghi chú đơn hàng</label>
                <textarea id="note"
                          name="note"
                          maxlength="500"
                          placeholder="Ví dụ: Giao hàng giờ hành chính"><c:out value="${note}"/></textarea>
                <div class="error">
                    <c:out value="${errors.note}"/>
                </div>

                <h2 style="margin-top:30px;">
                    Phương thức thanh toán
                </h2>

                <label class="payment-option">
                    <input type="radio"
                           name="paymentMethod"
                           value="COD"
                           ${empty paymentMethod || paymentMethod == 'COD' ? 'checked' : ''}>
                    <span>Thanh toán khi nhận hàng (COD)</span>
                </label>

                <label class="payment-option">
                    <input type="radio"
                           name="paymentMethod"
                           value="BANK_TRANSFER"
                           ${paymentMethod == 'BANK_TRANSFER' ? 'checked' : ''} ${!bankReady?'disabled':''}>
                    <img src="${pageContext.request.contextPath}/assets/images/bank-transfer.svg" width="42" height="42" alt="Ngân hàng"><span>Chuyển khoản ngân hàng · Quét QR</span>
                </label>

                <div class="error">
                    <c:out value="${errors.paymentMethod}"/>
                </div>

                <p class="notice">Chọn chuyển khoản để xem mã QR theo số tiền của đơn sau khi xác nhận đặt hàng. Admin kiểm tra giao dịch trước khi xác nhận đã thanh toán.</p>
                <c:if test="${!bankReady}"><p class="notice">Chuyển khoản chưa được cấu hình. Hiện bạn có thể chọn COD.</p></c:if>

            </section>

            <!-- TÓM TẮT ĐƠN HÀNG -->
            <section class="box">

                <h2>Đơn hàng của bạn</h2>

                <c:forEach var="item" items="${items}">
                    <div class="order-item">

                        <div>
                            <div class="product-name">
                                <c:out value="${item.productName}"/>
                            </div>

                            <div class="detail">
                                <c:out value="${item.variantName}"/>
                            </div>

                            <div class="detail">
                                Số lượng: ${item.quantity}
                            </div>
                        </div>

                        <div class="money">
                            <fmt:formatNumber
                                value="${item.lineTotal}"
                                pattern="#,##0"/> đ
                        </div>

                    </div>
                </c:forEach>

                <div class="total-row">
                    <span>Tạm tính</span>
                    <strong>
                        <fmt:formatNumber
                            value="${subtotal}"
                            pattern="#,##0"/> đ
                    </strong>
                </div>

                <div class="total-row">
                    <span>Phí vận chuyển</span>
                    <strong>
                        <fmt:formatNumber
                            value="${shippingFee}"
                            pattern="#,##0"/> đ
                    </strong>
                </div>

                <div class="total-row grand-total">
                    <span>Tổng cộng</span>
                    <span>
                        <fmt:formatNumber
                            value="${total}"
                            pattern="#,##0"/> đ
                    </span>
                </div>

                <button type="submit" class="submit-btn">
                    Xác nhận đặt hàng
                </button>

                <p class="notice">
                    Vui lòng kiểm tra thông tin và tổng tiền
                    trước khi xác nhận đơn hàng.
                </p>

            </section>

        </div>
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"></form>

</div>
<script src="${pageContext.request.contextPath}/assets/js/extensions.js" defer></script>
</body>
</html>
