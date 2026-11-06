<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../common/taglibs.jspf" %>
<c:if test="${empty publicPage}"><c:redirect url="/home"/></c:if>
<!DOCTYPE html><html lang="vi"><head><title>Khám phá sản phẩm | HomeTech</title><%@ include file="../common/head.jspf" %></head>
<body data-placeholder="<c:out value='${placeholderUrl}'/>">
<jsp:include page="../common/header.jsp"/><jsp:include page="../common/navbar.jsp"/>

<main id="main-content" class="ht-container page-content">
    <nav aria-label="Đường dẫn"><ol class="ht-breadcrumbs"><li class="ht-breadcrumb-item"><a href="<c:url value='/home'/>">Trang chủ</a></li><li class="ht-breadcrumb-item active" aria-current="page">Sản phẩm</li></ol></nav>
    <div class="page-heading"><span class="eyebrow">CHỌN TIỆN NGHI CHO RIÊNG BẠN</span><h1>Khám phá sản phẩm<span class="orange-dot">.</span></h1></div>
    <%@ include file="../common/catalog-status.jspf" %>
    <c:if test="${not catalogUnavailable}">
    <c:if test="${filter.normalized}"><div class="ht-notice ht-notice-info" role="status">Một số giá trị lọc không hợp lệ đã được chuẩn hóa. Vui lòng kiểm tra lại bộ lọc bên dưới.</div></c:if>
    <form id="catalog-filter" action="<c:url value='/products'/>" method="get" class="ht-grid ht-gap-roomy">
        <aside class="ht-desktop-quarter"><div class="filter-panel">
            <div class="ht-flex ht-space-between ht-align-center ht-bottom-large"><h2>Chọn sản phẩm</h2><a class="ht-small" href="<c:url value='/products'/>">Đặt lại</a></div>
            <label class="ht-label" for="filter-q">Tên sản phẩm</label><input class="ht-input ht-bottom-large" id="filter-q" type="search" name="q" maxlength="200" value="<c:out value='${filter.q}'/>" placeholder="Ví dụ: nồi chiên">
            <label class="ht-label" for="categoryId">Danh mục</label><select class="ht-select ht-bottom-large" name="categoryId" id="categoryId"><option value="">Tất cả danh mục</option><c:if test="${categoryUnavailable}"><option value="${filter.categoryId}" selected>Danh mục không khả dụng (ID ${filter.categoryId})</option></c:if><c:forEach items="${categories}" var="category"><option value="${category.categoryId}" ${filter.categoryId == category.categoryId ? 'selected' : ''}><c:out value="${category.categoryName}"/></option></c:forEach></select>
            <label class="ht-label" for="brandId">Thương hiệu</label><select class="ht-select ht-bottom-large" name="brandId" id="brandId"><option value="">Tất cả thương hiệu</option><c:if test="${brandUnavailable}"><option value="${filter.brandId}" selected>Thương hiệu không khả dụng (ID ${filter.brandId})</option></c:if><c:forEach items="${brands}" var="brand"><option value="${brand.brandId}" ${filter.brandId == brand.brandId ? 'selected' : ''}><c:out value="${brand.brandName}"/></option></c:forEach></select>
        <fieldset>
            <legend class="ht-label">Khoảng giá (VNĐ)</legend>

            <label class="ht-label ht-small" for="minPrice">Từ</label>
            <input class="ht-input ht-bottom-normal"
                   id="minPrice"
                   name="minPrice"
                   type="number"
                   min="0"
                   step="1"
                   value="${filter.minPrice}"
                   placeholder="Ví dụ: 10000">

            <label class="ht-label ht-small" for="maxPrice">Đến</label>
            <input class="ht-input"
                   id="maxPrice"
                   name="maxPrice"
                   type="number"
                   min="0"
                   step="1"
                   value="${filter.maxPrice}"
                   placeholder="Ví dụ: 100000">
        </fieldset>
            <button class="ht-button ht-button-primary ht-full-width ht-top-normal" type="submit">Áp dụng</button>
        </div></aside>
        <section class="ht-desktop-three-quarters" aria-label="Kết quả sản phẩm"><div class="results-toolbar"><p><strong>${totalProducts}</strong> sản phẩm<c:if test="${not empty filter.q}"> cho “<c:out value="${filter.q}"/>”</c:if></p><div class="sort-control"><label for="sort">Sắp xếp:</label><select class="ht-select" name="sort" id="sort"><option value="newest" ${filter.sort == 'newest' ? 'selected' : ''}>Mới nhất</option><option value="priceAsc" ${filter.sort == 'priceAsc' ? 'selected' : ''}>Giá tăng dần</option><option value="priceDesc" ${filter.sort == 'priceDesc' ? 'selected' : ''}>Giá giảm dần</option></select><button class="ht-button ht-button-outline ht-button-small" type="submit">Xem</button></div></div>
            <div class="ht-grid ht-gap-roomy"><c:forEach items="${products}" var="item"><div class="ht-half ht-wide-third"><%@ include file="../common/product-card.jspf" %></div></c:forEach></div>
            <c:if test="${empty products}"><div class="empty-state"><span class="empty-icon" aria-hidden="true">⌕</span><h2>Chưa tìm thấy sản phẩm</h2><p>Thử từ khóa khác hoặc mở rộng bộ lọc để có thêm lựa chọn.</p><a class="ht-button ht-button-primary" href="<c:url value='/products'/>">Xem tất cả sản phẩm</a></div></c:if>
            <c:if test="${totalPages > 1}"><nav class="ht-top-section" aria-label="Phân trang sản phẩm"><ul class="ht-pagination ht-center ht-wrap">
            <li class="ht-page-item ${filter.page == 1 ? 'disabled' : ''}"><c:choose><c:when test="${filter.page > 1}"><a class="ht-page-link" href="<c:out value='${previousUrl}'/>" aria-label="Trang trước">←</a></c:when><c:otherwise><span class="ht-page-link">←</span></c:otherwise></c:choose></li>
            <c:forEach items="${pageLinks}" var="link"><li class="ht-page-item ${link.number == filter.page ? 'active' : ''}"><a class="ht-page-link" href="<c:out value='${link.url}'/>" ${link.number == filter.page ? 'aria-current="page"' : ''}>${link.number}</a></li></c:forEach>
            <li class="ht-page-item ${filter.page == totalPages ? 'disabled' : ''}"><c:choose><c:when test="${filter.page < totalPages}"><a class="ht-page-link" href="<c:out value='${nextUrl}'/>" aria-label="Trang sau">→</a></c:when><c:otherwise><span class="ht-page-link">→</span></c:otherwise></c:choose></li>
            </ul><p class="ht-text-center ht-muted ht-small">Trang ${filter.page} / ${totalPages}</p></nav></c:if>
        </section>
    </form></c:if>
</main>
<jsp:include page="../common/footer.jsp"/></body></html>

