<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<nav class="ht-navigation ht-navigation-responsive site-navbar" aria-label="Điều hướng chính">
    <div class="ht-container">
        <a class="nav-catalog" href="<c:url value='/products'/>"><span aria-hidden="true">☰</span> Khám phá sản phẩm</a>
        <button class="ht-menu-toggle" type="button" data-menu-toggle="public-navigation" aria-controls="public-navigation" aria-expanded="false" aria-label="Mở danh mục"><span class="ht-menu-icon" aria-hidden="true"></span></button>
        <div class="ht-disclosure ht-navigation-panel" id="public-navigation"><div class="ht-navigation-links ht-desktop-indent">
            <a class="ht-nav-link ${activePage == 'home' ? 'active' : ''}" href="<c:url value='/home'/>">Trang chủ</a>
            <c:forEach items="${categories}" var="category"><a class="ht-nav-link ${filter.categoryId == category.categoryId ? 'active' : ''}" href="<c:url value='/products'><c:param name='categoryId' value='${category.categoryId}'/></c:url>"><c:out value="${category.categoryName}"/></a></c:forEach>
        </div></div>
    </div>
</nav>

