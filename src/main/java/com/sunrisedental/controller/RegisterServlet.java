package com.sunrisedental.controller;


import at.favre.lib.crypto.bcrypt.BCrypt;
import com.sunrisedental.dao.UserDAO;
import com.sunrisedental.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@WebServlet("/api/auth/register")
public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final List<String> VALID_ROLES = Arrays.asList("ADMIN", "RECEPTIONIST", "DENTIST", "PATIENT");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String role = request.getParameter("role");

        // 1. Null or Empty Check
        if (isNullOrEmpty(username) || isNullOrEmpty(email) || isNullOrEmpty(password) || isNullOrEmpty(role)) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "All fields (username, email, password, role) are required.");
            return;
        }

        username = username.trim();
        email = email.trim();
        role = role.trim().toUpperCase();

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid email address format.");
            return;
        }

        if (password.length() < 6) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Password must be at least 6 characters long.");
            return;
        }

        if (!VALID_ROLES.contains(role)) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid role. Allowed roles: ADMIN, RECEPTIONIST, DENTIST, PATIENT.");
            return;
        }

        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        // Create User & Save to DB
        User newUser = new User(username, email, hashedPassword, role);
        boolean isRegistered = userDAO.registerUser(newUser);

        if (isRegistered) {
            response.setStatus(HttpServletResponse.SC_CREATED); // HTTP 201
            response.getWriter().write(String.format(
                    "{\"status\": \"success\", \"message\": \"User registered successfully!\", \"data\": {\"username\": \"%s\", \"email\": \"%s\", \"role\": \"%s\"}}",
                    username, email, role
            ));
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Registration failed. Email or Username already exists.");
        }
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.getWriter().write(String.format("{\"status\": \"error\", \"message\": \"%s\"}", message));
    }
}