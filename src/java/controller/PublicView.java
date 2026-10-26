package controller;

import dal.*;
import jakarta.servlet.http.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.*;
import model.*;

final class PublicView {
    private PublicView() { }
    static void initialize(HttpServletRequest request, String activePage) {
        request.setAttribute("publicPage", true);
        request.setAttribute("activePage", activePage);
        request.setAttribute("categories", List.of());
        request.setAttribute("placeholderUrl", request.getContextPath() + "/assets/images/product-placeholder.svg");
        for (String name : List.of("loginPath", "registerPath", "logoutPath", "profilePath", "cartPath", "cartAddPath")) {
            String path = request.getServletContext().getInitParameter("public." + name);
            // Only explicit application-local routes; no external or protocol-relative redirect.
            if (path != null && path.matches("/[a-zA-Z0-9_/-]+") && !path.contains("//")) {
                request.setAttribute(name, request.getContextPath() + path);
            }
        }
    }
    static void categories(HttpServletRequest request) throws SQLException {
        request.setAttribute("categories", new CategoryDAO().findActive());
    }
    static void unavailable(HttpServletRequest request, HttpServletResponse response, SQLException ex) {
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        request.setAttribute("catalogUnavailable", true);
        String reason = switch (Objects.toString(ex.getSQLState(), "")) {
            case "HT001" -> "missing/invalid public database URL";
            case "HT002" -> "SQL Server JDBC driver missing";
            case "HT003" -> "local database configuration unreadable";
            case "28000" -> "SQL Server login rejected";
            default -> "database query/connection failed";
        };
        request.getServletContext().log("Public catalog unavailable: " + reason
                + "; SQLState=" + ex.getSQLState() + ", code=" + ex.getErrorCode());
    }
    static void images(HttpServletRequest request, List<Product> products) {
        for (Product p : products) p.setImageUrl(imageUrl(request.getContextPath(), p.getImageUrl()));
    }
    static String imageUrl(String context, String source) {
        String fallback = context + "/assets/images/product-placeholder.svg";
        if (source == null || source.isBlank()) return fallback;
        String value = source.strip();
        try {
            URI uri = new URI(value);
            if (uri.isAbsolute()) {
                if (("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                        && uri.getHost() != null && uri.getUserInfo() == null) return uri.toASCIIString();
                return fallback;
            }
            if (value.startsWith("//") || value.contains("\\") || value.contains("..") || value.contains("%")
                    || uri.getQuery() != null || uri.getFragment() != null) return fallback;
            return context + "/" + (value.startsWith("/") ? value.substring(1) : value);
        } catch (URISyntaxException ex) { return fallback; }
    }
}

