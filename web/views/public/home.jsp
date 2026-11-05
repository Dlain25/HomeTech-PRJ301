<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../common/taglibs.jspf" %>
<c:if test="${empty publicPage}"><c:redirect url="/home"/></c:if>
<!DOCTYPE html><html lang="vi"><head><title>HomeTech — Chạm tiện nghi, sống thảnh thơi</title><%@ include file="../common/head.jspf" %><link rel="stylesheet" href="<c:url value='/assets/css/homepage-hero.css'/>"></head>
<body data-placeholder="<c:out value='${placeholderUrl}'/>">
<jsp:include page="../common/header.jsp"/><jsp:include page="../common/navbar.jsp"/>

<main id="main-content">
    <section class="ht-container hero-wrap">
        <div id="home-hero" class="ht-banner ht-banner-animated" role="region" aria-roledescription="carousel" aria-label="Khám phá HomeTech">
            <div class="ht-banner-track">
                <div class="ht-banner-slide active"><div class="hero-panel hero-showcase">
                    <div class="hero-copy">
                        <span class="eyebrow">GIA DỤNG HIỆN ĐẠI · NÂNG TẦM KHÔNG GIAN SỐNG</span>
                        <h1>Nhà đẹp hơn.<br><span>Sống tiện hơn.</span></h1>
                        <p>Chọn thiết bị thông minh cho căn nhà gọn gàng,<br class="hero-desktop-break"> hiện đại và dễ chịu hơn mỗi ngày.</p>
                        <a class="ht-button ht-button-orange" href="<c:url value='/products'/>">Mua sắm ngay <span aria-hidden="true">↗</span></a>
                    </div>
                    <div class="hero-showcase-art">
                        <%-- Crop only the appliance area of the reference. All copy and the CTA above are real HTML. --%>
                        <svg viewBox="970 40 1157 644" preserveAspectRatio="xMidYMid slice" role="img" aria-label="Nồi chiên không dầu, ấm điện, robot hút bụi và thiết bị gia dụng trong không gian hiện đại" focusable="false">
                            <image href="<c:url value='/assets/images/hero-appliances-reference.png'/>" width="2172" height="724"/>
                        </svg>
                    </div>
                </div></div>
                <div class="ht-banner-slide"><div class="hero-panel hero-panel-alt hero-panel-text">
                    <div class="hero-copy"><span class="eyebrow">KHÁM PHÁ ĐIỀU MỚI MỖI NGÀY</span><h2>Chăm chút nhà.<br><span>Thảnh thơi sống.</span></h2><p>Tìm thiết bị theo nhu cầu, thương hiệu và ngân sách.<br>Mọi thông tin rõ ràng trong một nơi.</p><a class="ht-button ht-button-orange" href="<c:url value='/products'><c:param name='sort' value='newest'/></c:url>">Xem sản phẩm mới ↗</a></div>
                </div></div>
            </div>
            <div class="hero-controls"><button type="button" data-banner-target="home-hero" data-banner-step="-1" aria-label="Banner trước">←</button><span>KHÁM PHÁ HOMETECH</span><button type="button" data-banner-target="home-hero" data-banner-step="1" aria-label="Banner tiếp theo">→</button></div>
        </div>
    </section>
    <div class="ht-container"><div class="benefit-strip"><div><span class="benefit-icon">✓</span><span><strong>Dễ dàng lựa chọn</strong><small>Lọc theo nhu cầu của bạn</small></span></div><div><span class="benefit-icon">◇</span><span><strong>Thông tin minh bạch</strong><small>Giá và tồn kho theo phiên bản</small></span></div><div><span class="benefit-icon">⌂</span><span><strong>Tiện nghi mỗi ngày</strong><small>Gia dụng cho mọi góc nhà</small></span></div></div>
    <%@ include file="../common/catalog-status.jspf" %>
    <c:if test="${not catalogUnavailable}">
        <section class="catalog-section"><div class="section-heading"><div><h2>Nhà bạn cần gì hôm nay?</h2></div><a href="<c:url value='/products'/>">Tất cả danh mục ↗</a></div>
            <div class="ht-grid ht-gap-compact"><c:forEach items="${categories}" var="category" varStatus="loop"><div class="ht-half ht-desktop-quarter"><a class="category-tile category-tone-${loop.index % 4}" href="<c:url value='/products'><c:param name='categoryId' value='${category.categoryId}'/></c:url>"><span class="category-number">0${loop.count} <span>↗</span></span><h3><c:out value="${category.categoryName}"/></h3><p><c:out value="${category.description}"/></p></a></div></c:forEach></div>
            <c:if test="${empty categories}"><p class="empty-inline">Danh mục đang được cập nhật.</p></c:if>
        </section>
        <section class="catalog-section"><div class="section-heading"><div><h2>Sản phẩm nổi bật<span class="orange-dot">.</span></h2></div><a href="<c:url value='/products'/>">Xem tất cả ↗</a></div>
            <div class="ht-grid ht-gap-roomy"><c:forEach items="${featuredProducts}" var="item"><div class="ht-half ht-desktop-quarter"><%@ include file="../common/product-card.jspf" %></div></c:forEach></div>
            <c:if test="${empty featuredProducts}"><p class="empty-inline">Chưa có sản phẩm nổi bật. Khám phá các sản phẩm khác trong danh mục.</p></c:if>
        </section>
        <section class="lifestyle-banner"><div><span class="eyebrow">TIỆN NGHI CHO NHỊP SỐNG MỚI</span><h2>Sống thông minh.<br>Tận hưởng mỗi ngày.</h2></div><a class="ht-button ht-button-light" href="<c:url value='/products'/>">Tìm thiết bị phù hợp ↗</a></section>
        <section class="catalog-section"><div class="section-heading"><div><h2>Thêm lựa chọn, thêm tiện nghi</h2></div><a href="<c:url value='/products'><c:param name='sort' value='newest'/></c:url>">Xem sản phẩm mới ↗</a></div>
            <div class="ht-grid ht-gap-roomy"><c:forEach items="${newProducts}" var="item"><div class="ht-half ht-desktop-quarter"><%@ include file="../common/product-card.jspf" %></div></c:forEach></div>
            <c:if test="${empty newProducts}"><p class="empty-inline">Sản phẩm đang được cập nhật. Hẹn gặp lại bạn sớm!</p></c:if>
        </section>
    </c:if></div>
</main>
<jsp:include page="../common/footer.jsp"/></body></html>

