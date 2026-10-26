
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đổi mật khẩu - HomeTech</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            padding: 40px 15px;
            background: #f3f6fa;
            font-family: Arial, sans-serif;
            color: #253247;
        }

        .container {
            max-width: 520px;
            margin: auto;
        }

        h1 {
            color: #1769aa;
            text-align: center;
        }

        .box {
            background: white;
            padding: 32px;
            border-radius: 12px;
            box-shadow: 0 4px 18px rgba(0,0,0,.08);
            margin-top: 25px;
        }

        label {
            display: block;
            font-weight: bold;
            margin-top: 20px;
            margin-bottom: 8px;
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

        .error {
            color: #d93025;
            font-size: 13px;
            margin-top: 6px;
        }

        .error-box {
            background: #fde8e8;
            color: #a32121;
            padding: 12px;
            border-radius: 7px;
            margin-bottom: 15px;
        }

        .hint {
            font-size: 13px;
            color: #667085;
            line-height: 1.6;
            margin-top: 8px;
        }

        button {
            width: 100%;
            margin-top: 25px;
            padding: 14px;
            background: #1769aa;
            border: none;
            border-radius: 7px;
            color: white;
            font-size: 16px;
            font-weight: bold;
            cursor: pointer;
        }

        button:hover {
            background: #104d80;
        }

        .back {
            display: block;
            text-align: center;
            margin-top: 20px;
            color: #1769aa;
            text-decoration: none;
        }
    </style>
</head>

<body>

<div class="container">

    <h1>Đổi mật khẩu HomeTech</h1>

    <div class="box">

        <h2>Bảo mật tài khoản</h2>

        <p class="hint">
            Vui lòng nhập mật khẩu hiện tại và mật khẩu mới
            để cập nhật thông tin đăng nhập.
        </p>

        <c:if test="${not empty errors.general}">
            <div class="error-box">
                <c:out value="${errors.general}"/>
            </div>
        </c:if>

        <form method="post"
              action="${pageContext.request.contextPath}/change-password"
              accept-charset="UTF-8">

            <label for="oldPassword">
                Mật khẩu hiện tại *
            </label>

            <input type="password"
                   id="oldPassword"
                   name="oldPassword"
                   autocomplete="current-password"
                   required>

            <div class="error">
                <c:out value="${errors.oldPassword}"/>
            </div>

            <label for="newPassword">
                Mật khẩu mới *
            </label>

            <input type="password"
                   id="newPassword"
                   name="newPassword"
                   autocomplete="new-password"
                   required>

            <div class="error">
                <c:out value="${errors.newPassword}"/>
            </div>

            <p class="hint">
                Mật khẩu mới phải đáp ứng quy tắc bảo mật
                của hệ thống và khác mật khẩu hiện tại.
            </p>

            <label for="confirmPassword">
                Nhập lại mật khẩu mới *
            </label>

            <input type="password"
                   id="confirmPassword"
                   name="confirmPassword"
                   autocomplete="new-password"
                   required>

            <div class="error">
                <c:out value="${errors.confirmPassword}"/>
            </div>

            <button type="submit">
                Xác nhận đổi mật khẩu
            </button>

        </form>

        <a class="back"
           href="${pageContext.request.contextPath}/profile">
            Quay lại thông tin cá nhân
        </a>

    </div>

</div>

</body>
</html>
