package com.sunrisedental.controller;


import at.favre.lib.crypto.bcrypt.BCrypt;
import com.sunrisedental.dao.UserDAO;
import com.sunrisedental.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@WebServlet("/api/auth/register")
public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
//    private static final List<String> VALID_ROLES = Arrays.asList("ADMIN", "RECEPTIONIST", "DENTIST", "PATIENT");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) !=null) {
            sb.append(line);
        }

        String username = null;
        String email = null;
        String password = null;

        try {
            JSONObject json = new JSONObject(sb.toString());
            username = json.optString("username", null);
            email = json.optString("email", null);
            password = json.optString("password", null);
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body format.");
            return;
        }

            // 1. Null or Empty Check
        if (isNullOrEmpty(username) || isNullOrEmpty(email) || isNullOrEmpty(password)) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Username, email, and password are required.");
            return;
        }

        username = username.trim();
        email = email.trim();

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid email address format.");
            return;
        }

        if (password.length() < 6) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Password must be at least 6 characters long.");
            return;
        }




        String defaultRole = "PATIENT";
        String defaultStatus = "ACTIVE";

        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());

        // Create User Object with Default Role and Status
        User newUser = new User(username, email, hashedPassword, defaultRole, defaultStatus);
        boolean isRegistered = userDAO.registerUser(newUser);

        if (isRegistered) {
            response.setStatus(HttpServletResponse.SC_CREATED); // 201 Created
            response.getWriter().write(String.format(
                    "{\"status\": \"success\", \"message\": \"Patient registered successfully!\", \"data\": {\"username\": \"%s\", \"email\": \"%s\", \"role\": \"%s\"}}",
                    escapeJson(username), escapeJson(email), defaultRole
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
        response.getWriter().write(String.format("{\"status\": \"error\", \"message\": \"%s\"}", escapeJson(message)));
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}