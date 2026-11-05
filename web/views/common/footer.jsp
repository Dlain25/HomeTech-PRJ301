<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<footer class="site-footer" id="support">
    <div class="ht-container">
        <div class="ht-grid ht-gap-roomy">
            <div class="ht-desktop-five"><a class="footer-brand" href="<c:url value='/home'/>">Home<span>Tech</span></a></div>
            <div class="ht-half ht-desktop-quarter"><h2>Khám phá</h2><a href="<c:url value='/home'/>">Trang chủ</a><a href="<c:url value='/products'/>">Tất cả sản phẩm</a><a href="<c:url value='/products'><c:param name='sort' value='newest'/></c:url>">Sản phẩm mới</a></div>
            <div class="ht-half ht-desktop-third"><h2>Hỗ trợ khách hàng</h2><p>Thông tin giá, phiên bản và bảo hành có tại trang chi tiết của từng sản phẩm.</p><a href="<c:url value='/products'/>">Tìm sản phẩm phù hợp →</a></div>
        </div>
    </div>
</footer>

