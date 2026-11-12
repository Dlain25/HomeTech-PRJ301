<%@ page pageEncoding="UTF-8" %>
<%@ include file="tags.jspf" %><c:set var="pageTitle" value="Chuyển khoản ngân hàng"/><%@ include file="header.jspf" %>
<div class="ex-grid"><section class="ex-card ex-center"><h2>Quét mã để chuyển khoản</h2>
<c:choose><c:when test="${not empty qrUrl}"><img class="ex-qr" src="${fn:escapeXml(qrUrl)}" width="320" alt="Mã QR chuyển khoản HomeTech" referrerpolicy="no-referrer"><p class="ex-muted">Nếu ảnh không tải, dùng thông tin chuyển khoản bên cạnh.</p></c:when>
<c:when test="${not empty configError}"><p class="ex-notice error"><c:out value="${configError}"/></p></c:when><c:otherwise><p class="ex-notice">Yêu cầu hiện tại: <strong><c:out value="${labels[payment.status]}"/></strong>. Không cần chuyển khoản lại.</p></c:otherwise></c:choose></section>
<section class="ex-card"><span class="ex-badge"><c:out value="${payment.kind=='order'?'Đơn hàng':payment.kind=='topup'?'Nạp ví':'Đăng ký gói'}"/> #${payment.target_id}</span>
<h2 class="ex-amount"><fmt:formatNumber value="${payment.amount}" pattern="#,##0"/> đ</h2>
<dl><dt>Ngân hàng</dt><dd><c:out value="${bankId}"/></dd><dt>Tài khoản nhận</dt><dd><c:out value="${bankAccount}"/></dd><dt>Chủ tài khoản</dt><dd><c:out value="${bankName}"/></dd><dt>Nội dung chuyển khoản</dt><dd><strong><c:out value="${payment.transfer_code}"/></strong></dd></dl>
<p>Kiểm tra người nhận và số tiền trong ứng dụng ngân hàng. Sau khi chuyển, thông báo cho cửa hàng để được đối soát.</p>
<c:if test="${payment.status=='PENDING' && not empty qrUrl}"><form method="post" action="${cp}/payment/report"><input type="hidden" name="kind" value="${payment.kind}"><input type="hidden" name="id" value="${payment.target_id}"><button type="submit">Tôi đã chuyển khoản</button><input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}"></form><p class="ex-muted">Nút này gửi thông báo; giao dịch thành công sau khi admin xác nhận.</p></c:if>
<c:if test="${not empty payment.reported_at}"><p class="ex-notice success">Đã báo chuyển lúc <c:out value="${payment.reported_at_text}"/>. Vui lòng chờ kiểm tra.</p></c:if>
<a href="${cp}${payment.kind=='order'?'/orders':payment.kind=='topup'?'/wallet':'/services'}">← Quay lại danh sách</a></section></div>
<%@ include file="footer.jspf" %>
