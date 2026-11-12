<%@ page pageEncoding="UTF-8" %>
<%@ include file="tags.jspf" %><c:set var="pageTitle" value="Chưa thể thực hiện yêu cầu"/><%@ include file="header.jspf" %>
<section class="ex-card"><p role="alert"><c:out value="${message}"/></p><a class="ex-button" href="${cp}/home">Về trang chủ</a><a class="ex-button secondary" href="${cp}/orders">Đơn hàng của tôi</a></section>
<%@ include file="footer.jspf" %>
