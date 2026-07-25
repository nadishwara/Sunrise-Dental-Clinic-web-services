package com.sunrisedental.filter;


import com.sunrisedental.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebFilter("/api/*") // intercept
public class AuthenticationFilter implements Filter {

    private static final List<String> PUBLIC_URLS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/register-staff"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = requestURI.substring(contextPath.length());
//          check public URLs
            if (isPublicUrl(path)) {
                chain.doFilter(request, response);
                return;
            }
//            session check
            HttpSession session = httpRequest.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Please login to access this resource.");
                return;
            }

            User loggedInUser = (User) session.getAttribute("user");
            String role = loggedInUser.getRole();

            if (path.startsWith("/api/admin") && !"ADMIN".equalsIgnoreCase(role)) {
                sendErrorResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN, "Access Denied: Admin privileges required.");
                return;
            }

            if  (path.startsWith("/api/dentist") &&
            !("DENTIST".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role))) {
                sendErrorResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN, "Access Denied: Dentist or Admin privileges required.");
                return;
            }
            if (path.startsWith("/api/reception") &&
            !("RECEPTIONIST".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role))) {
                sendErrorResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN, "Access Denied: Reception or Admin privileges required.");
                return;
            }
            if (path.startsWith("/api/patient") && !("PATIENT").equalsIgnoreCase(role)) {
                sendErrorResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN, "Access Denied: Patient privileges required");
                return;
            }
            chain.doFilter(request, response);
        }
        private boolean isPublicUrl(String path) {
        String cleanPath = (path.length() > 1 && path.endsWith("/"))
                ? path.substring(0, path.length() -1)
                : path;
            return PUBLIC_URLS.stream().anyMatch(cleanPath::equalsIgnoreCase);
        }
    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format("{\"status\": \"error\", \"message\": \"%s\"}", message));
    }
}
