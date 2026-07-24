package com.sunrisedental.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.sunrisedental.dao.StaffDAO;
import com.sunrisedental.model.Staff;
import com.sunrisedental.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebServlet("/api/auth/register-staff")
public class RegisterStaffServlet extends HttpServlet {
    private final StaffDAO staffDAO = new StaffDAO();
    private static final List<String> STAFF_ROLES = Arrays.asList("ADMIN", "RECEPTIONIST", "DENTIST");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String role = request.getParameter("role");
        String fullName = request.getParameter("fullName");
        String contactNo = request.getParameter("contactNo");
        String specialization = request.getParameter("specialization");

        if (isEmpty(username) || isEmpty(email) || isEmpty(password) || isEmpty(role) || isEmpty(fullName) || isEmpty(contactNo) || isEmpty(specialization)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\": \"error\", \"message\": \"Required fields are missing!\"}");
            return;
        }
        role = role.trim().toUpperCase();
        if (!STAFF_ROLES.contains(role)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\": \"error\", \"message\": \"Invalid staff role. Allowed: ADMIN, RECEPTIONIST, DENTIST\"}");
            return;
        }
        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());

        User user = new User(username.trim(), email.trim(), hashedPassword, role);
        Staff staff = new Staff(0, fullName.trim(), contactNo, specialization);

        boolean isSuccess = staffDAO.registerStaff(user, staff);
        if(isSuccess) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("{\"status\": \"success\", \"message\": \"Staff registered successfully!\"}");
        } else {
            response.getWriter().write("{\"status\": \"error\", \"message\": \"Staff registration failed. Email or Username might exist.\"}");
        }
    }
    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
