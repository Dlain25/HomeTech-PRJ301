
<%@page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thông tin cá nhân - HomeTech</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            padding: 35px 15px;
            background: #f3f6fa;
            font-family: Arial, sans-serif;
            color: #253247;
        }

        .container {
            max-width: 650px;
            margin: auto;
        }

        h1 {
            color: #1769aa;
            margin-bottom: 10px;
        }

        .box {
            background: white;
            padding: 30px;
            margin-top: 25px;
            border-radius: 12px;
            box-shadow: 0 4px 18px rgba(0,0,0,.07);
        }

        h2 {
            font-size: 21px;
            color: #173b66;
            margin-top: 0;
        }

        label {
            display: block;
            font-weight: bold;
            margin-top: 19px;
            margin-bottom: 7px;
        }

        input {
            width: 100%;
            padding: 12px;
            border: 1px solid #ccd5df;
            border-radius: 7px;
            font-size: 15px;
        }

        input:focus {
            outline: none;
            border-color: #1769aa;
        }

        input:disabled {
            background: #f1f3f5;
            color: #667085;
        }

        .hint {
            color: #667085;
            font-size: 13px;
            margin-top: 6px;
        }

        .error {
            color: #d93025;
            font-size: 13px;
            margin-top: 6px;
        }

        .message {
            padding: 14px;
            border-radius: 7px;
            margin-bottom: 18px;
        }

        .success {
            background: #d4edda;
            color: #155724;
        }

        .error-box {
            background: #f8d7da;
            color: #721c24;
        }

        button {
            width: 100%;
            padding: 14px;
            background: #1769aa;
            color: white;
            border: none;
            border-radius: 7px;
            font-size: 16px;
            font-weight: bold;
            cursor: pointer;
            margin-top: 25px;
        }

        button:hover {
            background: #104d80;
        }

        a {
            text-decoration: none;
            color: #1769aa;
        }

        .links {
            display: flex;
            justify-content: space-between;
            gap: 15px;
            margin-top: 22px;
            flex-wrap: wrap;
        }
    </style>
</head>

<body>

<div class="container">

    <h1>Tài khoản HomeTech</h1>

    <a href="${pageContext.request.contextPath}/">
        Quay về trang chủ
    </a>

    <div class="box">

        <h2>Thông tin cá nhân</h2>

        <c:if test="${not empty sessionScope.flashSuccess}">
            <div class="message success">
                <c:out value="${sessionScope.flashSuccess}"/>
            </div>
            <c:remove var="flashSuccess" scope="session"/>
        </c:if>

        <c:if test="${not empty errors.general}">
            <div class="message error-box">
                <c:out value="${errors.general}"/>
            </div>
        </c:if>

        <form method="post"
              action="${pageContext.request.contextPath}/profile"
              accept-charset="UTF-8">

            <label for="username">Tên đăng nhập</label>
            <input type="text"
                   id="username"
                   value="<c:out value='${username}'/>"
                   disabled>

            <p class="hint">
                Tên đăng nhập không thể thay đổi.
            </p>

            <label for="fullName">Họ và tên *</label>
            <input type="text"
                   id="fullName"
                   name="fullName"
                   value="<c:out value='${fullName}'/>"
                   maxlength="120"
                   required>

            <div class="error">
                <c:out value="${errors.fullName}"/>
            </div>

            <label for="email">Email *</label>
            <input type="email"
                   id="email"
                   name="email"
                   value="<c:out value='${email}'/>"
                   maxlength="254"
                   required>

            <div class="error">
                <c:out value="${errors.email}"/>
            </div>

            <label for="phone">Số điện thoại</label>
            <input type="tel"
                   id="phone"
                   name="phone"
                   value="<c:out value='${phone}'/>"
                   maxlength="20">

            <div class="error">
                <c:out value="${errors.phone}"/>
            </div>

            <button type="submit">
                Lưu thay đổi
            </button>

        </form>

        <div class="links">
            <a href="${pageContext.request.contextPath}/change-password">
                Đổi mật khẩu
            </a>

            <a href="${pageContext.request.contextPath}/orders">
                Lịch sử đơn hàng
            </a>
        </div>
                
<form action="${pageContext.request.contextPath}/logout"
      method="post"
      onsubmit="return confirm('Bạn muốn đăng xuất khỏi HomeTech?');">

    <button type="submit"
            style="background-color: #dc3545;">
        Đăng xuất
    </button>

</form>

    </div>

</div>

</body>
</html>
