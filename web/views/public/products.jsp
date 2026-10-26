<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="../common/taglibs.jspf" %>
<c:if test="${empty publicPage}"><c:redirect url="/home"/></c:if>
<!DOCTYPE html><html lang="vi"><head><title>Khám phá sản phẩm | HomeTech</title><%@ include file="../common/head.jspf" %></head>
<body data-placeholder="<c:out value='${placeholderUrl}'/>">
<jsp:include page="../common/header.jsp"/><jsp:include page="../common/navbar.jsp"/>

<main id="main-content" class="container page-content">
    <nav aria-label="Đường dẫn"><ol class="breadcrumb"><li class="breadcrumb-item"><a href="<c:url value='/home'/>">Trang chủ</a></li><li class="breadcrumb-item active" aria-current="page">Sản phẩm</li></ol></nav>
    <div class="page-heading"><span class="eyebrow">CHỌN TIỆN NGHI CHO RIÊNG BẠN</span><h1>Khám phá sản phẩm<span class="orange-dot">.</span></h1></div>
    <%@ include file="../common/catalog-status.jspf" %>
    <c:if test="${not catalogUnavailable}">
    <c:if test="${filter.normalized}"><div class="alert alert-info" role="status">Một số giá trị lọc không hợp lệ đã được chuẩn hóa. Vui lòng kiểm tra lại bộ lọc bên dưới.</div></c:if>
    <form id="catalog-filter" action="<c:url value='/products'/>" method="get" class="row g-4">
        <aside class="col-lg-3"><div class="filter-panel">
            <div class="d-flex justify-content-between align-items-center mb-4"><h2>Chọn sản phẩm</h2><a class="small" href="<c:url value='/products'/>">Đặt lại</a></div>
            <label class="form-label" for="filter-q">Tên sản phẩm</label><input class="form-control mb-4" id="filter-q" type="search" name="q" maxlength="200" value="<c:out value='${filter.q}'/>" placeholder="Ví dụ: nồi chiên">
            <label class="form-label" for="categoryId">Danh mục</label><select class="form-select mb-4" name="categoryId" id="categoryId"><option value="">Tất cả danh mục</option><c:if test="${categoryUnavailable}"><option value="${filter.categoryId}" selected>Danh mục không khả dụng (ID ${filter.categoryId})</option></c:if><c:forEach items="${categories}" var="category"><option value="${category.categoryId}" ${filter.categoryId == category.categoryId ? 'selected' : ''}><c:out value="${category.categoryName}"/></option></c:forEach></select>
            <label class="form-label" for="brandId">Thương hiệu</label><select class="form-select mb-4" name="brandId" id="brandId"><option value="">Tất cả thương hiệu</option><c:if test="${brandUnavailable}"><option value="${filter.brandId}" selected>Thương hiệu không khả dụng (ID ${filter.brandId})</option></c:if><c:forEach items="${brands}" var="brand"><option value="${brand.brandId}" ${filter.brandId == brand.brandId ? 'selected' : ''}><c:out value="${brand.brandName}"/></option></c:forEach></select>
            <fieldset><legend class="form-label">Khoảng giá (VNĐ)</legend>
                <label class="form-label small" for="minPrice">Từ</label>
                <div class="price-control mb-3">
                    <input class="form-control" id="minPrice" type="text" inputmode="numeric" name="minPrice" pattern="[0-9]{1,16}" maxlength="22" value="<c:out value='${filter.minPrice}'/>" placeholder="Từ" data-vnd-price>
                    <div class="price-stepper"><button type="button" data-price-target="minPrice" data-price-step="1" aria-label="Tăng giá từ thêm 100.000 đồng" disabled>▲</button><button type="button" data-price-target="minPrice" data-price-step="-1" aria-label="Giảm giá từ bớt 100.000 đồng" disabled>▼</button></div>
                </div>
                <label class="form-label small" for="maxPrice">Đến</label>
                <div class="price-control">
                    <input class="form-control" id="maxPrice" type="text" inputmode="numeric" name="maxPrice" pattern="[0-9]{1,16}" maxlength="22" value="<c:out value='${filter.maxPrice}'/>" placeholder="Đến" data-vnd-price>
                    <div class="price-stepper"><button type="button" data-price-target="maxPrice" data-price-step="1" aria-label="Tăng giá đến thêm 100.000 đồng" disabled>▲</button><button type="button" data-price-target="maxPrice" data-price-step="-1" aria-label="Giảm giá đến bớt 100.000 đồng" disabled>▼</button></div>
                </div>
            </fieldset>
            <button class="btn btn-primary w-100 mt-3" type="submit">Áp dụng</button>
        </div></aside>
        <section class="col-lg-9" aria-label="Kết quả sản phẩm"><div class="results-toolbar"><p><strong>${totalProducts}</strong> sản phẩm<c:if test="${not empty filter.q}"> cho “<c:out value="${filter.q}"/>”</c:if></p><div class="sort-control"><label for="sort">Sắp xếp:</label><select class="form-select" name="sort" id="sort"><option value="newest" ${filter.sort == 'newest' ? 'selected' : ''}>Mới nhất</option><option value="priceAsc" ${filter.sort == 'priceAsc' ? 'selected' : ''}>Giá tăng dần</option><option value="priceDesc" ${filter.sort == 'priceDesc' ? 'selected' : ''}>Giá giảm dần</option></select><button class="btn btn-outline-primary btn-sm" type="submit">Xem</button></div></div>
            <div class="row g-4"><c:forEach items="${products}" var="item"><div class="col-6 col-xl-4"><%@ include file="../common/product-card.jspf" %></div></c:forEach></div>
            <c:if test="${empty products}"><div class="empty-state"><span class="empty-icon" aria-hidden="true">⌕</span><h2>Chưa tìm thấy sản phẩm</h2><p>Thử từ khóa khác hoặc mở rộng bộ lọc để có thêm lựa chọn.</p><a class="btn btn-primary" href="<c:url value='/products'/>">Xem tất cả sản phẩm</a></div></c:if>
            <c:if test="${totalPages > 1}"><nav class="mt-5" aria-label="Phân trang sản phẩm"><ul class="pagination justify-content-center flex-wrap">
            <li class="page-item ${filter.page == 1 ? 'disabled' : ''}"><c:choose><c:when test="${filter.page > 1}"><a class="page-link" href="<c:out value='${previousUrl}'/>" aria-label="Trang trước">←</a></c:when><c:otherwise><span class="page-link">←</span></c:otherwise></c:choose></li>
            <c:forEach items="${pageLinks}" var="link"><li class="page-item ${link.number == filter.page ? 'active' : ''}"><a class="page-link" href="<c:out value='${link.url}'/>" ${link.number == filter.page ? 'aria-current="page"' : ''}>${link.number}</a></li></c:forEach>
            <li class="page-item ${filter.page == totalPages ? 'disabled' : ''}"><c:choose><c:when test="${filter.page < totalPages}"><a class="page-link" href="<c:out value='${nextUrl}'/>" aria-label="Trang sau">→</a></c:when><c:otherwise><span class="page-link">→</span></c:otherwise></c:choose></li>
            </ul><p class="text-center text-secondary small">Trang ${filter.page} / ${totalPages}</p></nav></c:if>
        </section>
    </form></c:if>
</main>
<jsp:include page="../common/footer.jsp"/></body></html>

