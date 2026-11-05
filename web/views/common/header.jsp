<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<a class="skip-link" href="#main-content">Đến nội dung chính</a>
<header class="site-header">
    <div class="ht-container header-inner">
        <a class="brand" href="<c:url value='/home'/>" aria-label="HomeTech - Trang chủ"><span class="brand-mark" aria-hidden="true">H<span>·</span></span><span>Home<span class="brand-tech">Tech</span></span></a>
        <form class="header-search" action="<c:url value='/products'/>" method="get" role="search">
            <label for="header-search" class="ht-screen-reader">Tìm theo tên sản phẩm hoặc SKU</label>
            <input id="header-search" type="search" name="q" maxlength="200" value="<c:out value='${filter.q}'/>" placeholder="Bạn đang tìm thiết bị gì?">
            <button type="submit" aria-label="Tìm kiếm">⌕</button>
        </form>
        <div class="header-actions">
            <c:if test="${not empty sessionScope.user and sessionScope.user.roleId == 1 and sessionScope.user.status == 'ACTIVE'}"><a href="<c:url value='/admin/dashboard'/>">Quản trị</a></c:if>
            <c:choose>
                <c:when test="${not empty sessionScope.user}">
                    <a href="<c:url value='/profile'/>"><c:out value="${empty sessionScope.user.fullName ? sessionScope.user.username : sessionScope.user.fullName}"/></a>
                    <a href="<c:url value='/orders'/>">Đơn hàng</a>
                    <form method="post" action="<c:url value='/logout'/>"><button type="submit">Đăng xuất</button></form>
                </c:when>
                <c:otherwise><a href="<c:url value='/login'/>">Đăng nhập</a><a href="<c:url value='/register'/>">Đăng ký</a></c:otherwise>
            </c:choose>
            <a class="cart-link" href="<c:url value='/cart'/>">Giỏ hàng</a>
        </div>
    </div>
</header>

