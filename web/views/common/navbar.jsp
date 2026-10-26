<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<nav class="navbar navbar-expand-lg site-navbar" aria-label="Điều hướng chính">
    <div class="container">
        <a class="nav-catalog" href="<c:url value='/products'/>"><span aria-hidden="true">☰</span> Khám phá sản phẩm</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#public-navigation" aria-controls="public-navigation" aria-expanded="false" aria-label="Mở danh mục"><span class="navbar-toggler-icon"></span></button>
        <div class="collapse navbar-collapse" id="public-navigation"><div class="navbar-nav ms-lg-4">
            <a class="nav-link ${activePage == 'home' ? 'active' : ''}" href="<c:url value='/home'/>">Trang chủ</a>
            <c:forEach items="${categories}" var="category"><a class="nav-link ${filter.categoryId == category.categoryId ? 'active' : ''}" href="<c:url value='/products'><c:param name='categoryId' value='${category.categoryId}'/></c:url>"><c:out value="${category.categoryName}"/></a></c:forEach>
        </div></div>
    </div>
</nav>

