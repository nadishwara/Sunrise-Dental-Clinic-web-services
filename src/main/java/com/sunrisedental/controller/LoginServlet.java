package com.sunrisedental.controller;

import com.sunrisedental.dao.UserDAO;
import com.sunrisedental.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/api/auth/login")
public class LoginServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            response.getWriter().write("{\"status\": \"error\", \"message\": \"Email and password are required fields.\"}");
            return;
        }
        email = email.trim();

        User user = userDAO.authenticateUser(email, password);

        if (user != null) {
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession session = request.getSession(true);

            session.setMaxInactiveInterval(3600);

            session.setAttribute("user", user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("role", user.getRole());

            if (user.getStaffId() > 0) {
                session.setAttribute("staffId", user.getStaffId());
            }

            Cookie sessionCookie = new Cookie("JSESSIONID", session.getId());
            sessionCookie.setPath("/");
            sessionCookie.setHttpOnly(true);
            response.addCookie(sessionCookie);

//            response.setHeader("Set-Cookie", String.format("JSESSIONID=%s; Path=/; HttpOnly; SameSite=Lax", session.getId()));

            response.setStatus(HttpServletResponse.SC_OK);
            String jsonResponse = String.format(
                    "{\"status\": \"success\", \"message\": \"Login successful\", \"data\": {\"userId\": %d, \"username\": \"%s\", \"role\": \"%s\", \"staffId\": %s}}",
                    user.getUserId(),
                    user.getUsername(),
                    user.getRole(),
                    (user.getStaffId() > 0 ? String.valueOf(user.getStaffId()) : "null")
            ); // 200
            response.getWriter().write(jsonResponse);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.getWriter().write("{\"status\": \"error\", \"message\": \"Invalid email or password. Please try again.\"}");
        }
    }
}