<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../common/taglibs.jspf" %>
<c:if test="${empty publicPage}"><c:redirect url="/home"/></c:if>
<!DOCTYPE html><html lang="vi"><head><title><c:out value='${empty product ? "Chi tiết sản phẩm" : product.productName}'/> | HomeTech</title><%@ include file="../common/head.jspf" %></head>
<body data-placeholder="<c:out value='${placeholderUrl}'/>">
<jsp:include page="../common/header.jsp"/><jsp:include page="../common/navbar.jsp"/>

<main id="main-content" class="ht-container page-content">
    <nav aria-label="Đường dẫn"><ol class="ht-breadcrumbs"><li class="ht-breadcrumb-item"><a href="<c:url value='/home'/>">Trang chủ</a></li><li class="ht-breadcrumb-item"><a href="<c:url value='/products'/>">Sản phẩm</a></li><c:if test="${not empty product}"><li class="ht-breadcrumb-item active" aria-current="page"><c:out value="${product.productName}"/></li></c:if></ol></nav>
    <%@ include file="../common/catalog-status.jspf" %>
    <c:if test="${productNotFound}"><div class="empty-state"><span class="eyebrow">404 · HOMETECH</span><h1>Không tìm thấy sản phẩm</h1><p>Sản phẩm không tồn tại hoặc hiện đã ngừng hiển thị.</p><a class="ht-button ht-button-primary" href="<c:url value='/products'/>">Tiếp tục khám phá</a></div></c:if>
    <c:if test="${not catalogUnavailable and not empty product}">
    <div class="ht-grid ht-gap-roomy product-detail">
        <section class="ht-desktop-half" aria-label="Hình ảnh sản phẩm"><div class="detail-main-image"><img id="main-product-image" src="<c:out value='${product.imageUrl}'/>" alt="<c:out value='${product.productName}'/>" width="600" height="500" data-product-image></div>
            <c:if test="${not empty productImages}"><div class="image-thumbnails"><c:forEach items="${productImages}" var="image" varStatus="loop"><button type="button" class="thumbnail ${loop.first ? 'is-selected' : ''}" aria-label="Xem ảnh ${loop.count}" aria-pressed="${loop.first}" data-gallery-src="<c:out value='${image.imageUrl}'/>"><img src="<c:out value='${image.imageUrl}'/>" alt="Ảnh ${loop.count}" width="80" height="80" data-product-image></button></c:forEach></div></c:if>
        </section>
        <section class="ht-desktop-half"><span class="product-brand"><c:out value="${product.brandName}"/></span><h1><c:out value="${product.productName}"/></h1><p class="detail-sku">SKU: <c:out value="${product.sku}"/> <span>·</span> <a href="<c:url value='/products'><c:param name='categoryId' value='${product.categoryId}'/></c:url>"><c:out value="${product.categoryName}"/></a></p>
            <div class="detail-price-box"><div class="detail-price" id="variant-price" aria-live="polite"><c:choose><c:when test="${not empty selectedVariant}"><fmt:formatNumber value="${selectedVariant.price}" maxFractionDigits="0"/> ₫</c:when><c:otherwise>Chưa có giá bán</c:otherwise></c:choose></div><span>Giá của phiên bản được chọn</span></div>
            <c:choose><c:when test="${not empty variants}">
                <form id="variant-form" method="post" data-cart-ready="${not empty cartAddPath}" <c:if test="${not empty cartAddPath}">action="<c:out value='${cartAddPath}'/>"</c:if>>
                    <input type="hidden" name="action" value="add">
                    <label class="ht-label ht-top-large" for="variant-select">Phiên bản &amp; màu sắc</label>
                    <select class="ht-select" id="variant-select" name="variantId"><c:forEach items="${variants}" var="variant"><option value="${variant.variantId}" data-price-label="<fmt:formatNumber value='${variant.price}' maxFractionDigits='0'/> ₫" data-stock="${variant.quantityAvailable}" data-sku="<c:out value='${variant.variantSku}'/>"><c:out value="${variant.variantName}"/><c:if test="${not empty variant.color}"> — <c:out value="${variant.color}"/></c:if> · <fmt:formatNumber value="${variant.price}" maxFractionDigits="0"/> ₫ · ${variant.quantityAvailable > 0 ? 'Còn hàng' : 'Hết hàng'}</option></c:forEach></select>
                    <p class="ht-small ht-muted ht-top-small">Mã phiên bản: <span id="variant-sku"><c:out value="${selectedVariant.variantSku}"/></span></p>
                    <p id="variant-stock" class="stock-label ${selectedVariant.quantityAvailable > 0 ? 'in-stock' : 'out-stock'}" aria-live="polite"><c:choose><c:when test="${selectedVariant.quantityAvailable > 0}">Còn hàng · ${selectedVariant.quantityAvailable} sản phẩm</c:when><c:otherwise>Hết hàng</c:otherwise></c:choose></p>
                    <div class="purchase-row"><div><label class="ht-label" for="quantity">Số lượng</label><input class="ht-input" id="quantity" name="quantity" type="number" min="1" max="${selectedVariant.quantityAvailable}" step="1" value="1" required ${selectedVariant.quantityAvailable == 0 ? 'disabled' : ''}></div><button id="add-to-cart" class="ht-button ht-button-orange" type="submit" disabled>Thêm vào giỏ hàng</button></div>
                    <c:if test="${not empty publicCsrfParameter and not empty publicCsrfToken}"><input type="hidden" name="<c:out value='${publicCsrfParameter}'/>" value="<c:out value='${publicCsrfToken}'/>"></c:if>
                    <c:if test="${empty cartAddPath}"><p class="integration-note">Tính năng mua hàng sẽ sớm được mở. Bạn có thể tham khảo phiên bản, giá và tồn kho tại đây.</p></c:if>
                    <noscript><p class="integration-note">Bật JavaScript để xem giá và tồn kho khi đổi phiên bản. Thao tác mua hiện bị vô hiệu hóa.</p></noscript>
                </form>
            </c:when><c:otherwise><div class="ht-notice ht-notice-muted ht-top-large">Sản phẩm chưa có phiên bản khả dụng.</div><button class="ht-button ht-button-muted" disabled>Chưa thể thêm vào giỏ hàng</button></c:otherwise></c:choose>
            <div class="warranty-note"><span aria-hidden="true">✓</span><div><strong>Bảo hành ${product.warrantyMonths} tháng</strong><small>Thông tin bảo hành theo sản phẩm</small></div></div>
        </section>
    </div>
    <section class="description-panel"><span class="eyebrow">HIỂU HƠN VỀ SẢN PHẨM</span><h2>Thông tin chi tiết</h2><div class="ht-grid ht-gap-roomy"><div class="ht-desktop-seven"><p class="product-description"><c:out value="${empty product.description ? 'Thông tin mô tả đang được cập nhật.' : product.description}"/></p></div><div class="ht-desktop-five"><dl class="spec-list"><div><dt>Thương hiệu</dt><dd><c:out value="${product.brandName}"/></dd></div><div><dt>Danh mục</dt><dd><c:out value="${product.categoryName}"/></dd></div><div><dt>Mã sản phẩm</dt><dd><c:out value="${product.sku}"/></dd></div><div><dt>Bảo hành</dt><dd>${product.warrantyMonths} tháng</dd></div></dl></div></div></section>
    <section class="catalog-section"><div class="section-heading"><div><span class="eyebrow">THÊM LỰA CHỌN CHO BẠN</span><h2>Có thể bạn cũng thích</h2></div><a href="<c:url value='/products'><c:param name='categoryId' value='${product.categoryId}'/></c:url>">Xem cùng danh mục ↗</a></div><div class="ht-grid ht-gap-roomy"><c:forEach items="${relatedProducts}" var="item"><div class="ht-half ht-desktop-quarter"><%@ include file="../common/product-card.jspf" %></div></c:forEach></div><c:if test="${empty relatedProducts}"><p class="empty-inline">Chưa có sản phẩm liên quan trong danh mục này.</p></c:if></section>
    </c:if>
</main>
<jsp:include page="../common/footer.jsp"/></body></html>

