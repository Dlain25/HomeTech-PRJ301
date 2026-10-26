<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../common/taglibs.jspf" %>
<c:if test="${empty publicPage}"><c:redirect url="/home"/></c:if>
<!DOCTYPE html><html lang="vi"><head><title>HomeTech — Chạm tiện nghi, sống thảnh thơi</title><%@ include file="../common/head.jspf" %></head>
<body data-placeholder="<c:out value='${placeholderUrl}'/>">
<jsp:include page="../common/header.jsp"/><jsp:include page="../common/navbar.jsp"/>

<main id="main-content">
    <section class="container hero-wrap">
        <div id="home-hero" class="carousel slide" aria-label="Khám phá HomeTech">
            <div class="carousel-inner">
                <div class="carousel-item active"><div class="hero-panel">
                    <div class="hero-copy"><span class="eyebrow">TỔ ẤM THÔNG MINH · CUỘC SỐNG THẢNH THƠI</span><h1>Tiện nghi hơn.<br><span>Yêu nhà hơn.</span></h1><p>Những thiết bị nhỏ, thay đổi lớn.<br>Khám phá gia dụng cho nhịp sống của bạn.</p><a class="btn btn-orange" href="<c:url value='/products'/>">Khám phá ngay <span aria-hidden="true">↗</span></a><div class="hero-caption">TỪ CĂN BẾP ĐẾN MỌI GÓC NHÀ</div></div>
                    <div class="hero-visual"><img src="<c:url value='/assets/images/hero-home.svg'/>" alt="Minh họa không gian sống với các thiết bị gia dụng" width="620" height="460"><span class="hero-note"><span class="note-dot"></span> Thiết kế cho mỗi ngày</span></div>
                </div></div>
                <div class="carousel-item"><div class="hero-panel hero-panel-alt">
                    <div class="hero-copy"><span class="eyebrow">KHÁM PHÁ ĐIỀU MỚI MỖI NGÀY</span><h2>Chăm chút nhà.<br><span>Thảnh thơi sống.</span></h2><p>Tìm thiết bị theo nhu cầu, thương hiệu và ngân sách.<br>Mọi thông tin rõ ràng trong một nơi.</p><a class="btn btn-orange" href="<c:url value='/products'><c:param name='sort' value='newest'/></c:url>">Xem sản phẩm mới ↗</a></div>
                    <div class="hero-visual"><img src="<c:url value='/assets/images/hero-home.svg'/>" alt="Minh họa thiết bị cho ngôi nhà hiện đại" width="620" height="460"></div>
                </div></div>
            </div>
            <div class="hero-controls"><button type="button" data-bs-target="#home-hero" data-bs-slide="prev" aria-label="Banner trước">←</button><span>KHÁM PHÁ HOMETECH</span><button type="button" data-bs-target="#home-hero" data-bs-slide="next" aria-label="Banner tiếp theo">→</button></div>
        </div>
    </section>
    <div class="container"><div class="benefit-strip"><div><span class="benefit-icon">✓</span><span><strong>Dễ dàng lựa chọn</strong><small>Lọc theo nhu cầu của bạn</small></span></div><div><span class="benefit-icon">◇</span><span><strong>Thông tin minh bạch</strong><small>Giá và tồn kho theo phiên bản</small></span></div><div><span class="benefit-icon">⌂</span><span><strong>Tiện nghi mỗi ngày</strong><small>Gia dụng cho mọi góc nhà</small></span></div></div>
    <%@ include file="../common/catalog-status.jspf" %>
    <c:if test="${not catalogUnavailable}">
        <section class="catalog-section"><div class="section-heading"><div><span class="eyebrow">BẮT ĐẦU TỪ NHU CẦU CỦA BẠN</span><h2>Nhà bạn cần gì hôm nay?</h2></div><a href="<c:url value='/products'/>">Tất cả danh mục ↗</a></div>
            <div class="row g-3"><c:forEach items="${categories}" var="category" varStatus="loop"><div class="col-6 col-lg-3"><a class="category-tile category-tone-${loop.index % 4}" href="<c:url value='/products'><c:param name='categoryId' value='${category.categoryId}'/></c:url>"><span class="category-number">0${loop.count} <span>↗</span></span><h3><c:out value="${category.categoryName}"/></h3><p><c:out value="${category.description}"/></p></a></div></c:forEach></div>
            <c:if test="${empty categories}"><p class="empty-inline">Danh mục đang được cập nhật.</p></c:if>
        </section>
        <section class="catalog-section"><div class="section-heading"><div><span class="eyebrow">GỢI Ý CHO TỔ ẤM</span><h2>Sản phẩm nổi bật<span class="orange-dot">.</span></h2></div><a href="<c:url value='/products'/>">Xem tất cả ↗</a></div>
            <div class="row g-4"><c:forEach items="${featuredProducts}" var="item"><div class="col-6 col-lg-3"><%@ include file="../common/product-card.jspf" %></div></c:forEach></div>
            <c:if test="${empty featuredProducts}"><p class="empty-inline">Chưa có sản phẩm nổi bật. Khám phá các sản phẩm khác trong danh mục.</p></c:if>
        </section>
        <section class="lifestyle-banner"><div><span class="eyebrow">NHÀ LÀ NƠI ĐỂ TẬN HƯỞNG</span><h2>Bớt việc nhà.<br>Thêm thời gian cho bạn.</h2></div><a class="btn btn-light" href="<c:url value='/products'/>">Tìm thiết bị phù hợp ↗</a></section>
        <section class="catalog-section"><div class="section-heading"><div><span class="eyebrow">MỚI TẠI HOMETECH</span><h2>Thêm lựa chọn, thêm tiện nghi</h2></div><a href="<c:url value='/products'><c:param name='sort' value='newest'/></c:url>">Xem sản phẩm mới ↗</a></div>
            <div class="row g-4"><c:forEach items="${newProducts}" var="item"><div class="col-6 col-lg-3"><%@ include file="../common/product-card.jspf" %></div></c:forEach></div>
            <c:if test="${empty newProducts}"><p class="empty-inline">Sản phẩm đang được cập nhật. Hẹn gặp lại bạn sớm!</p></c:if>
        </section>
    </c:if></div>
</main>
<jsp:include page="../common/footer.jsp"/></body></html>

