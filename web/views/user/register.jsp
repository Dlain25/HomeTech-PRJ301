<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng ký - HomeTech</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            font-family: Arial, sans-serif;
            background: #f3f6fa;
            margin: 0;
            padding: 40px 15px;
        }

        .register-box {
            max-width: 480px;
            margin: auto;
            background: white;
            padding: 30px;
            border-radius: 12px;
            box-shadow: 0 4px 18px rgba(0,0,0,0.1);
        }

        h2 {
            text-align: center;
            color: #1769aa;
            margin-top: 0;
        }

        label {
            display: block;
            margin-top: 15px;
            margin-bottom: 6px;
            font-weight: bold;
        }

        input {
            width: 100%;
            padding: 11px;
            border: 1px solid #ccc;
            border-radius: 6px;
            font-size: 14px;
        }

        button {
            width: 100%;
            padding: 13px;
            margin-top: 22px;
            border: none;
            border-radius: 6px;
            background: #1769aa;
            color: white;
            font-size: 16px;
            cursor: pointer;
        }

        button:hover {
            background: #105184;
        }

        .error {
            color: #d93025;
            font-size: 13px;
            margin-top: 5px;
        }

        .bottom {
            text-align: center;
            margin-top: 20px;
        }

        a {
            color: #1769aa;
            text-decoration: none;
        }
    </style>
</head>

<body>

<div class="register-box">

    <h2>Đăng ký tài khoản HomeTech</h2>

    <c:if test="${not empty errors.general}">
        <p class="error"><c:out value="${errors.general}"/></p>
    </c:if>

    <form action="${pageContext.request.contextPath}/register"
          method="post">

        <label>Tên đăng nhập</label>
        <input type="text"
               name="username"
               value="<c:out value='${username}'/>"
               required>
        <div class="error"><c:out value="${errors.username}"/></div>

        <label>Họ và tên</label>
        <input type="text"
               name="fullName"
               value="<c:out value='${fullName}'/>"
               required>
        <div class="error"><c:out value="${errors.fullName}"/></div>

        <label>Email</label>
        <input type="email"
               name="email"
               value="<c:out value='${email}'/>"
               required>
        <div class="error"><c:out value="${errors.email}"/></div>

        <label>Số điện thoại</label>
        <input type="tel"
               name="phone"
               value="<c:out value='${phone}'/>">
        <div class="error"><c:out value="${errors.phone}"/></div>

        <label>Mật khẩu</label>
        <input type="password"
               name="password"
               required>
        <div class="error"><c:out value="${errors.password}"/></div>

        <label>Nhập lại mật khẩu</label>
        <input type="password"
               name="confirmPassword"
               required>
        <div class="error"><c:out value="${errors.confirmPassword}"/></div>

        <button type="submit">Đăng ký</button>

    </form>

    <div class="bottom">
        Đã có tài khoản?
        <a href="${pageContext.request.contextPath}/login">
            Đăng nhập
        </a>
    </div>

</div>

</body>
</html>