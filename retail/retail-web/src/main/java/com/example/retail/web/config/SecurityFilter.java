package com.example.retail.web.config;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebFilter("/*")
public class SecurityFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        boolean isLoggedIn = (session != null && session.getAttribute("loggedInUser") != null);
        boolean isLoginPage = path.equals("/login.xhtml");
        boolean isAccessDeniedPage = path.equals("/access-denied.xhtml");
        boolean isResource = path.startsWith("/javax.faces.resource/") ||
                path.startsWith("/resources/");

        // Rediriger vers login si non authentifié et page protégée
        if (!isLoggedIn && !isLoginPage && !isResource && !isAccessDeniedPage && isProtectedPage(path)) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.xhtml");
            return;
        }

        // Vérifier les rôles pour les zones spécifiques
        if (isLoggedIn && !isAccessDeniedPage && !hasRequiredRole(httpRequest, path)) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/access-denied.xhtml");
            return;
        }

        // Rediriger vers accueil si déjà authentifié et tentative de login
        if (isLoggedIn && isLoginPage) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/home.xhtml");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isProtectedPage(String path) {
        return path.startsWith("/admin/") ||
                path.startsWith("/gerant/") ||
                path.startsWith("/fournisseur/") ||
                path.startsWith("/logistique/") ||
                path.equals("/home.xhtml") ||
                path.equals("/lots.xhtml");
    }

    private boolean hasRequiredRole(HttpServletRequest request, String path) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;

        @SuppressWarnings("unchecked")
        List<String> userRoles = (List<String>) session.getAttribute("userRoles");
        if (userRoles == null) return false;

        if (path.startsWith("/admin/")) {
            return userRoles.contains("ADMIN");
        } else if (path.startsWith("/gerant/")) {
            return userRoles.contains("GERANT") || userRoles.contains("ADMIN") || userRoles.contains("GESTIONNAIRE");
        } else if (path.startsWith("/fournisseur/")) {
            return userRoles.contains("FOURNISSEUR") || userRoles.contains("GERANT") || userRoles.contains("ADMIN");
        } else if (path.startsWith("/logistique/")) {
            return userRoles.contains("LOGISTIQUE") || userRoles.contains("GERANT") || userRoles.contains("ADMIN");
        }

        return true; // Pages non spécifiques
    }
}