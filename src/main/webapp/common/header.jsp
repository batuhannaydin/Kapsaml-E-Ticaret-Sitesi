<%-- Tüm müşteri sayfalarında ortak üst kısım: head + navbar + flash mesaj alanı --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="tr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <%-- Sayfa başlığı: pageTitle attribute'u set edilmediyse varsayılanı göster --%>
    <title><c:out value="${pageTitle != null ? pageTitle : 'E-Ticaret Portali'}"/></title>
    <%-- Bootstrap 5 (CDN) ve proje stil dosyası --%>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body>
<%-- Üst menü: marka, ürün listesi, arama formu, sepet, giriş/çıkış --%>
<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container">
        <a class="navbar-brand fw-bold" href="${pageContext.request.contextPath}/">
            E-Ticaret
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNav">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="mainNav">
            <ul class="navbar-nav me-auto">
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/products">Tum Urunler</a>
                </li>
                <%-- Sadece giriş yapmış kullanıcıya Siparişlerim linkini göster --%>
                <c:if test="${not empty sessionScope.currentUser}">
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/my-orders">Siparislerim</a>
                    </li>
                </c:if>
            </ul>
            <form class="d-flex me-3" action="${pageContext.request.contextPath}/search" method="get" role="search">
                <input class="form-control form-control-sm" type="search" name="q" placeholder="Urun ara..."
                       value="<c:out value='${searchKeyword}'/>">
                <button class="btn btn-sm btn-outline-light ms-2" type="submit">Ara</button>
            </form>
            <ul class="navbar-nav">
                <%-- Sepet linki: içinde ürün varsa adet rozeti (badge) gösterilir --%>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/cart">
                        Sepet
                        <c:if test="${not empty sessionScope.cart && sessionScope.cart.size() > 0}">
                            <span class="badge bg-warning text-dark">${sessionScope.cart.size()}</span>
                        </c:if>
                    </a>
                </li>
                <%-- Giriş yapmış kullanıcıya "Merhaba + Çıkış", aksi halde "Giriş/Kayıt" bağlantıları --%>
                <c:choose>
                    <c:when test="${not empty sessionScope.currentUser}">
                        <li class="nav-item">
                            <span class="nav-link disabled text-light">
                                Merhaba, <c:out value="${sessionScope.currentUser.fullName}"/>
                            </span>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/logout">Cikis</a>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/login">Giris</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/register">Kayit Ol</a>
                        </li>
                    </c:otherwise>
                </c:choose>
            </ul>
        </div>
    </div>
</nav>
<main class="container my-4">
    <%-- Tek seferlik flash mesaj (örk "Kayıt başarılı"). Göster ve sessiondan sil. --%>
    <c:if test="${not empty sessionScope.flashMessage}">
        <div class="alert alert-success">
            <c:out value="${sessionScope.flashMessage}"/>
        </div>
        <c:remove var="flashMessage" scope="session"/>
    </c:if>
