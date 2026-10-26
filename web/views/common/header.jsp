
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!-- Skip to main content -->
<a class="skip-link" href="#main-content">
    Đến nội dung chính
</a>

<!-- TOP BAR -->
<div class="topbar">
    <div class="container d-flex justify-content-between gap-3">

        <span>
            Tiện nghi mỗi ngày · Trọn vẹn tổ ấm
        </span>

        <span class="d-none d-md-inline">
            HomeTech — Gia dụng cho cuộc sống hiện đại
        </span>

    </div>
</div>

<!-- MAIN HEADER -->
<header class="site-header">

    <div class="container header-inner">

        <!-- LOGO HOMETECH -->
        <a class="brand"
           href="<c:url value='/home'/>"
           aria-label="HomeTech - Trang chủ">

            <span class="brand-mark" aria-hidden="true">
                H<span>·</span>
            </span>

            <span>
                Home<span class="brand-tech">Tech</span>

                <small>
                    CHẠM TIỆN NGHI. SỐNG THẢNH THƠI.
                </small>
            </span>

        </a>

        <!-- SEARCH BAR -->
        <form class="header-search"
              action="<c:url value='/products'/>"
              method="get"
              role="search">

            <label for="header-search"
                   class="visually-hidden">
                Tìm theo tên sản phẩm hoặc SKU
            </label>

            <input id="header-search"
                   type="search"
                   name="q"
                   maxlength="200"
                   value="<c:out value='${filter.q}'/>"
                   placeholder="Bạn đang tìm thiết bị gì?">

            <button type="submit"
                    aria-label="Tìm kiếm">
                ⌕
            </button>

        </form>

        <!-- ACCOUNT AND CART -->
        <div class="header-actions">
            
<%-- Chỉ ADMIN mới thấy nút Quản trị --%>
<c:if test="${not empty sessionScope.user
              and sessionScope.user.roleId == 1
              and sessionScope.user.status == 'ACTIVE'}">

    <a href="<c:url value='/admin/dashboard'/>"
       title="Mở trang quản trị HomeTech"
       style="background:#173b66;
              color:white;
              padding:10px 16px;
              border-radius:7px;
              text-decoration:none;
              font-weight:bold;">
        Quản trị
    </a>

</c:if>

            <c:choose>

                <%-- NGƯỜI DÙNG ĐÃ ĐĂNG NHẬP --%>
                <c:when test="${not empty sessionScope.user}">

                    <!-- PROFILE -->
                    <a href="<c:url value='/profile'/>"
                       title="Thông tin tài khoản">

                        <c:choose>
                            <c:when test="${not empty sessionScope.user.fullName}">
                                <c:out value="${sessionScope.user.fullName}"/>
                            </c:when>

                            <c:otherwise>
                                Tài khoản
                            </c:otherwise>
                        </c:choose>

                    </a>

                    <!-- LOGOUT -->
                    <form action="<c:url value='/logout'/>"
                          method="post"
                          class="logout-form">

                        <button type="submit"
                                title="Đăng xuất HomeTech">
                            Đăng xuất
                        </button>

                    </form>

                </c:when>

                <%-- NGƯỜI DÙNG CHƯA ĐĂNG NHẬP --%>
                <c:otherwise>

                    <!-- LOGIN -->
                    <a href="<c:url value='/login'/>"
                       title="Đăng nhập HomeTech">
                        Đăng nhập
                    </a>

                    <!-- REGISTER -->
                    <a href="<c:url value='/register'/>"
                       title="Đăng ký tài khoản">
                        Đăng ký
                    </a>

                </c:otherwise>

            </c:choose>

            <!-- SHOPPING CART -->
            <a class="cart-link"
               href="<c:url value='/cart'/>"
               title="Xem giỏ hàng">
                Giỏ hàng
            </a>

        </div>

    </div>

</header>
