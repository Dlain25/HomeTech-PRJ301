
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Đăng nhập | HomeTech</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f3f6fb;
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            margin: 0;
        }
        .login-box {
            width: 360px;
            max-width: calc(100% - 48px);
            padding: 32px;
            background: white;
            border-radius: 12px;
            box-shadow: 0 8px 30px rgba(0,0,0,.08);
        }
        h1 { text-align: center; color: #165dce; }
        label { display: block; margin: 16px 0 6px; }
        input {
            width: 100%;
            padding: 12px;
            box-sizing: border-box;
            border: 1px solid #ccc;
            border-radius: 6px;
        }
        button {
            width: 100%;
            margin-top: 22px;
            padding: 12px;
            border: none;
            border-radius: 6px;
            background: #165dce;
            color: white;
            cursor: pointer;
        }
        .error { color: #c62828; margin-top: 14px; }
        .back { display: block; text-align: center; margin-top: 20px; }
    </style>
</head>
<body>
    <div class="login-box">
        <h1>HomeTech</h1>
        <h2>Đăng nhập</h2>

        <c:if test="${not empty error}">
            <p class="error"><c:out value="${error}"/></p>
        </c:if>

        <form action="${pageContext.request.contextPath}/login" method="post">
            <label for="username">Tên đăng nhập</label>
            <input type="text" id="username" name="username"
                   value="<c:out value='${username}'/>"
                   autocomplete="username" required>

            <label for="password">Mật khẩu</label>
            <input type="password" id="password" name="password"
                   autocomplete="current-password" required>

            <button type="submit">Đăng nhập</button>
        </form>

        <a class="back" href="${pageContext.request.contextPath}/home">
            Quay lại trang chủ
        </a>
    </div>
</body>
</html>
