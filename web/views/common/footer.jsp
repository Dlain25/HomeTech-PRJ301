<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<footer class="site-footer" id="support">
    <div class="container">
        <div class="row g-4">
            <div class="col-lg-5"><a class="footer-brand" href="<c:url value='/home'/>">Home<span>Tech</span></a><p class="mt-3 footer-intro">Mang tiện nghi đến gần hơn với mỗi gia đình.<br>Khám phá thiết bị phù hợp cho tổ ấm của bạn.</p></div>
            <div class="col-6 col-lg-3"><h2>Khám phá</h2><a href="<c:url value='/home'/>">Trang chủ</a><a href="<c:url value='/products'/>">Tất cả sản phẩm</a><a href="<c:url value='/products'><c:param name='sort' value='newest'/></c:url>">Sản phẩm mới</a></div>
            <div class="col-6 col-lg-4"><h2>Hỗ trợ khách hàng</h2><p>Thông tin giá, phiên bản và bảo hành có tại trang chi tiết của từng sản phẩm.</p><a href="<c:url value='/products'/>">Tìm sản phẩm phù hợp →</a></div>
        </div>
    </div>
</footer>

