
<%@page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<c:url value="/admin/products" var="productUrl"/>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý sản phẩm - HomeTech Admin</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            background: #f3f6fa;
            color: #263648;
            font-family: Arial, sans-serif;
        }

        .top {
            background: #193b65;
            color: white;
            padding: 26px 5%;
        }

        .top h1 {
            margin: 0 0 8px;
            font-size: 30px;
        }

        .top p {
            margin: 0;
            color: #d9e5f3;
        }

        .nav {
            display: flex;
            gap: 26px;
            flex-wrap: wrap;
            padding: 17px 5%;
            background: white;
            border-bottom: 1px solid #ddd;
        }

        .nav a {
            text-decoration: none;
            color: #193b65;
            font-weight: bold;
        }

        .nav a.active {
            color: #1769aa;
            text-decoration: underline;
        }

        .container {
            max-width: 1400px;
            margin: 32px auto;
            padding: 0 20px;
        }

        .box, .product-card {
            background: white;
            padding: 25px;
            border-radius: 12px;
            margin-bottom: 25px;
            box-shadow: 0 3px 12px rgba(0,0,0,.05);
        }

        h2, h3 {
            color: #193b65;
        }

        h2 {
            margin-top: 0;
        }

        .grid {
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            gap: 16px;
        }

        .field {
            display: flex;
            flex-direction: column;
            gap: 7px;
        }

        label {
            font-weight: bold;
            font-size: 14px;
        }

        input, select, textarea {
            padding: 11px;
            width: 100%;
            border: 1px solid #ccd6e0;
            border-radius: 7px;
            font-family: inherit;
            font-size: 14px;
            min-width: 0;
        }

        textarea {
            resize: vertical;
            min-height: 75px;
        }

        button {
            background: #1769aa;
            color: white;
            border: none;
            border-radius: 7px;
            padding: 11px 17px;
            cursor: pointer;
            font-size: 14px;
            font-weight: bold;
        }

        button:hover {
            background: #104f83;
        }

        .btn-green {
            background: #16804d;
        }

        .btn-green:hover {
            background: #116238;
        }

        .btn-red {
            background: #cb3443;
        }

        .btn-red:hover {
            background: #a52634;
        }

        .btn-gray {
            background: #596b7c;
        }

        .actions {
            margin-top: 18px;
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
            align-items: center;
        }

        .message {
            padding: 14px 18px;
            border-radius: 8px;
            margin-bottom: 20px;
        }

        .success {
            background: #d9f1e2;
            color: #155b32;
        }

        .error {
            background: #fde5e5;
            color: #9b2525;
        }

        .product-heading {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            gap: 16px;
            flex-wrap: wrap;
        }

        .product-heading h3 {
            margin: 0 0 8px;
        }

        .meta {
            color: #617184;
            font-size: 14px;
            line-height: 1.7;
        }

        .status {
            display: inline-block;
            padding: 7px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: bold;
        }

        .status-active {
            background: #d9f1e2;
            color: #155b32;
        }

        .status-inactive {
            background: #fde5e5;
            color: #9b2525;
        }

        .variant-box {
            background: #f3f7fc;
            border: 1px solid #e0e8f0;
            border-radius: 9px;
            padding: 17px;
            margin-top: 18px;
        }

        .variant-stats {
            display: flex;
            gap: 24px;
            flex-wrap: wrap;
            margin-top: 10px;
            font-size: 14px;
        }

        .variant-stats strong {
            color: #1769aa;
        }

        details {
            margin-top: 15px;
            border: 1px solid #dce4ec;
            border-radius: 8px;
            padding: 14px;
        }

        summary {
            cursor: pointer;
            color: #193b65;
            font-weight: bold;
        }

        details form {
            margin-top: 18px;
        }

        .search {
            margin: 15px 0 24px;
            max-width: 480px;
        }

        .hint {
            color: #667588;
            font-size: 13px;
            line-height: 1.5;
        }

        .inline-form {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
            align-items: center;
        }

        .inline-form input {
            max-width: 150px;
        }

        .section-divider {
            border: 0;
            border-top: 1px solid #e5eaf0;
            margin: 22px 0;
        }

        .empty {
            text-align: center;
            padding: 35px;
            background: white;
            border-radius: 10px;
        }

        @media (max-width: 850px) {
            .grid {
                grid-template-columns: 1fr;
            }

            .top h1 {
                font-size: 24px;
            }

            .box, .product-card {
                padding: 18px;
            }
        }
    </style>
</head>

<body>

<header class="top">
    <h1>HomeTech Admin - Product Management</h1>
    <p>Thêm, sửa, quản lý giá và tồn kho sản phẩm</p>
</header>

<nav class="nav">
    <a href="${pageContext.request.contextPath}/admin/dashboard">
        Dashboard
    </a>
    <a href="${pageContext.request.contextPath}/admin/categories">
        Categories
    </a>
    <a href="${productUrl}" class="active">
        Products
    </a>
    <a href="${pageContext.request.contextPath}/admin/users">
        Users
    </a>
    <a href="${pageContext.request.contextPath}/home">
        Về trang chủ
    </a>
</nav>

<main class="container">

    <!-- THONG BAO -->
    <c:if test="${not empty sessionScope.adminProductSuccess}">
        <div class="message success">
            <c:out value="${sessionScope.adminProductSuccess}"/>
        </div>
        <c:remove var="adminProductSuccess" scope="session"/>
    </c:if>

    <c:if test="${not empty sessionScope.adminProductError}">
        <div class="message error">
            <c:out value="${sessionScope.adminProductError}"/>
        </div>
        <c:remove var="adminProductError" scope="session"/>
    </c:if>

    <!-- THEM SAN PHAM -->
    <section class="box">
        <h2>Thêm sản phẩm mới</h2>

        <p class="hint">
            Khi thêm sản phẩm, hệ thống tự tạo một phiên bản
            tiêu chuẩn và bản ghi tồn kho.
        </p>

        <form method="post" action="${productUrl}">

            <input type="hidden" name="csrfToken"
                   value="<c:out value='${csrfToken}'/>">
            <input type="hidden" name="action" value="add">

            <div class="grid">

                <div class="field">
                    <label>Tên sản phẩm *</label>
                    <input type="text" name="productName"
                           maxlength="200" required
                           placeholder="Ví dụ: Máy giặt Samsung">
                </div>

                <div class="field">
                    <label>SKU sản phẩm *</label>
                    <input type="text" name="sku"
                           maxlength="60" required
                           placeholder="Ví dụ: MG-SAMSUNG-01">
                </div>

                <div class="field">
                    <label>Danh mục *</label>
                    <select name="categoryId" required>
                        <option value="">-- Chọn danh mục --</option>
                        <c:forEach var="cat" items="${categories}">
                            <option value="${cat.id}">
                                <c:out value="${cat.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="field">
                    <label>Thương hiệu *</label>
                    <select name="brandId" required>
                        <option value="">-- Chọn thương hiệu --</option>
                        <c:forEach var="brand" items="${brands}">
                            <option value="${brand.id}">
                                <c:out value="${brand.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="field">
                    <label>Giá bán ban đầu (VNĐ) *</label>
                    <input type="number" name="price"
                           min="0" step="0.01" required
                           placeholder="Ví dụ: 1500000">
                </div>

                <div class="field">
                    <label>Tồn kho ban đầu *</label>
                    <input type="number" name="stock"
                           min="0" step="1" value="0" required>
                </div>

                <div class="field">
                    <label>Bảo hành (tháng) *</label>
                    <input type="number" name="warrantyMonths"
                           min="0" step="1" value="12" required>
                </div>

                <div class="field">
                    <label>URL ảnh sản phẩm</label>
                    <input type="text" name="imageUrl"
                           placeholder="/assets/images/product.jpg">
                </div>

                <div class="field">
                    <label>Mô tả sản phẩm</label>
                    <textarea name="description"
                              placeholder="Nhập mô tả sản phẩm"></textarea>
                </div>

            </div>

            <div class="actions">
                <button class="btn-green" type="submit">
                    + Thêm sản phẩm
                </button>
            </div>
        </form>
    </section>

    <!-- DANH SACH SAN PHAM -->
    <section>
        <h2>Danh sách sản phẩm</h2>

        <input class="search" id="productSearch"
               type="search"
               placeholder="Tìm theo tên sản phẩm hoặc SKU...">

        <c:if test="${empty products}">
            <div class="empty">
                Chưa có sản phẩm nào.
            </div>
        </c:if>

        <c:forEach var="p" items="${products}">

            <article class="product-card searchable-product">

                <div class="product-heading">

                    <div>
                        <h3>
                            #${p.productId} -
                            <c:out value="${p.productName}"/>
                        </h3>

                        <div class="meta">
                            SKU:
                            <strong><c:out value="${p.sku}"/></strong>
                            |
                            <c:out value="${p.categoryName}"/>
                            |
                            <c:out value="${p.brandName}"/>
                        </div>
                    </div>

                    <span class="status ${p.status == 'ACTIVE' ? 'status-active' : 'status-inactive'}">
                        <c:out value="${p.status}"/>
                    </span>

                </div>

                <p class="meta">
                    Giá cơ bản:
                    <strong>
                        <fmt:formatNumber value="${p.basePrice}"
                                          pattern="#,##0"/> đ
                    </strong>
                    |
                    Bảo hành: ${p.warrantyMonths} tháng
                </p>

                <!-- PHIEN BAN -->
                <c:if test="${not empty p.variantId}">

                    <div class="variant-box">

                        <strong>
                            Phiên bản #${p.variantId}:
                            <c:out value="${p.variantName}"/>
                        </strong>

                        <div class="meta">
                            SKU phiên bản:
                            <c:out value="${p.variantSku}"/>

                            <c:if test="${not empty p.color}">
                                | Màu: <c:out value="${p.color}"/>
                            </c:if>
                        </div>

                        <div class="variant-stats">
                            <span>
                                Giá bán:
                                <strong>
                                    <fmt:formatNumber value="${p.price}"
                                                      pattern="#,##0"/> đ
                                </strong>
                            </span>

                            <span>
                                Tồn kho:
                                <strong>${p.stock}</strong>
                            </span>

                            <span>
                                Trạng thái:
                                <strong>
                                    ${p.variantActive == true ? 'Đang bán' : 'Ngừng bán'}
                                </strong>
                            </span>
                        </div>

                        <!-- CAP NHAT TON KHO -->
                        <details>
                            <summary>Cập nhật tồn kho</summary>

                            <form method="post"
                                  action="${productUrl}"
                                  class="inline-form">

                                <input type="hidden" name="csrfToken"
                                       value="<c:out value='${csrfToken}'/>">
                                <input type="hidden" name="action"
                                       value="stock">
                                <input type="hidden" name="productId"
                                       value="${p.productId}">
                                <input type="hidden" name="variantId"
                                       value="${p.variantId}">

                                <input type="number" name="stock"
                                       min="0" step="1"
                                       value="${p.stock}" required>

                                <button type="submit">
                                    Lưu tồn kho
                                </button>
                            </form>

                            <p class="hint">
                                Đây là số lượng tồn kho mới,
                                không phải số lượng cộng thêm.
                            </p>
                        </details>

                        <!-- SUA PHIEN BAN VA GIA -->
                        <details>
                            <summary>Sửa phiên bản và giá bán</summary>

                            <form method="post"
                                  action="${productUrl}">

                                <input type="hidden" name="csrfToken"
                                       value="<c:out value='${csrfToken}'/>">
                                <input type="hidden" name="action"
                                       value="variant">
                                <input type="hidden" name="productId"
                                       value="${p.productId}">
                                <input type="hidden" name="variantId"
                                       value="${p.variantId}">

                                <div class="grid">

                                    <div class="field">
                                        <label>SKU phiên bản</label>
                                        <input name="variantSku"
                                               value="<c:out value='${p.variantSku}'/>"
                                               required>
                                    </div>

                                    <div class="field">
                                        <label>Tên phiên bản</label>
                                        <input name="variantName"
                                               value="<c:out value='${p.variantName}'/>"
                                               required>
                                    </div>

                                    <div class="field">
                                        <label>Màu sắc</label>
                                        <input name="color"
                                               value="<c:out value='${p.color}'/>">
                                    </div>

                                    <div class="field">
                                        <label>Giá bán phiên bản (VNĐ)</label>
                                        <input name="price" type="number"
                                               min="0" step="0.01"
                                               value="${p.price}" required>
                                    </div>

                                    <div class="field">
                                        <label>Trạng thái phiên bản</label>
                                        <select name="active">
                                            <option value="true"
                                                ${p.variantActive == true ? 'selected' : ''}>
                                                Đang bán
                                            </option>
                                            <option value="false"
                                                ${p.variantActive == false ? 'selected' : ''}>
                                                Ngừng bán
                                            </option>
                                        </select>
                                    </div>

                                </div>

                                <div class="actions">
                                    <button type="submit">
                                        Lưu phiên bản
                                    </button>
                                </div>

                            </form>
                        </details>

                    </div>

                </c:if>

                <!-- SUA SAN PHAM -->
                <details>
                    <summary>Sửa thông tin sản phẩm</summary>

                    <form method="post" action="${productUrl}">

                        <input type="hidden" name="csrfToken"
                               value="<c:out value='${csrfToken}'/>">
                        <input type="hidden" name="action"
                               value="update">
                        <input type="hidden" name="productId"
                               value="${p.productId}">

                        <div class="grid">

                            <div class="field">
                                <label>Tên sản phẩm</label>
                                <input name="productName"
                                       value="<c:out value='${p.productName}'/>"
                                       required>
                            </div>

                            <div class="field">
                                <label>SKU sản phẩm</label>
                                <input name="sku"
                                       value="<c:out value='${p.sku}'/>"
                                       required>
                            </div>

                            <div class="field">
                                <label>Danh mục</label>
                                <select name="categoryId" required>
                                    <c:forEach var="cat" items="${categories}">
                                        <option value="${cat.id}"
                                            ${cat.id == p.categoryId ? 'selected' : ''}>
                                            <c:out value="${cat.name}"/>
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="field">
                                <label>Thương hiệu</label>
                                <select name="brandId" required>
                                    <c:forEach var="brand" items="${brands}">
                                        <option value="${brand.id}"
                                            ${brand.id == p.brandId ? 'selected' : ''}>
                                            <c:out value="${brand.name}"/>
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="field">
                                <label>Giá cơ bản (VNĐ)</label>
                                <input name="basePrice"
                                       type="number"
                                       min="0" step="0.01"
                                       value="${p.basePrice}" required>
                            </div>

                            <div class="field">
                                <label>Bảo hành (tháng)</label>
                                <input name="warrantyMonths"
                                       type="number" min="0"
                                       value="${p.warrantyMonths}" required>
                            </div>

                            <div class="field">
                                <label>Mô tả sản phẩm</label>
                                <textarea name="description"><c:out value="${p.description}"/></textarea>
                            </div>

                        </div>

                        <div class="actions">
                            <button type="submit">
                                Lưu thông tin sản phẩm
                            </button>
                        </div>

                        <p class="hint">
                            Thay đổi giá cơ bản không tự động
                            thay đổi giá bán của từng phiên bản.
                        </p>

                    </form>
                </details>

                <!-- THAY ANH -->
                <details>
                    <summary>Thay ảnh đại diện</summary>

                    <form method="post" action="${productUrl}">

                        <input type="hidden" name="csrfToken"
                               value="<c:out value='${csrfToken}'/>">
                        <input type="hidden" name="action"
                               value="image">
                        <input type="hidden" name="productId"
                               value="${p.productId}">

                        <div class="field">
                            <label>URL ảnh mới</label>
                            <input name="imageUrl"
                                   type="text"
                                   placeholder="Nhập URL ảnh mới"
                                   required>
                        </div>

                        <p class="hint">
                            Ảnh hiện tại:
                            <c:out value="${p.imageUrl}"
                                   default="Chưa có ảnh"/>
                        </p>

                        <div class="actions">
                            <button type="submit">
                                Cập nhật ảnh
                            </button>
                        </div>
                    </form>
                </details>

                <hr class="section-divider">

                <!-- BAT / TAT SAN PHAM -->
                <form method="post" action="${productUrl}"
                      onsubmit="return confirm('Bạn chắc chắn muốn thay đổi trạng thái sản phẩm?');">

                    <input type="hidden" name="csrfToken"
                           value="<c:out value='${csrfToken}'/>">
                    <input type="hidden" name="action"
                           value="status">
                    <input type="hidden" name="productId"
                           value="${p.productId}">

                    <c:choose>
                        <c:when test="${p.status == 'ACTIVE'}">
                            <input type="hidden" name="status"
                                   value="INACTIVE">
                            <button type="submit" class="btn-red">
                                Ngừng bán sản phẩm
                            </button>
                        </c:when>
                        <c:otherwise>
                            <input type="hidden" name="status"
                                   value="ACTIVE">
                            <button type="submit" class="btn-green">
                                Bật bán sản phẩm
                            </button>
                        </c:otherwise>
                    </c:choose>

                </form>

            </article>

        </c:forEach>

    </section>

</main>

<script>
    const search = document.getElementById("productSearch");

    search.addEventListener("input", function () {
        const keyword = this.value.toLowerCase().trim();

        document.querySelectorAll(".searchable-product")
                .forEach(function (card) {
                    const text = card.textContent.toLowerCase();
                    card.style.display =
                            text.includes(keyword) ? "" : "none";
                });
    });
</script>

</body>
</html>
