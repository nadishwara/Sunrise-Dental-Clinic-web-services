package com.sunrisedental.controller;

import com.sunrisedental.dao.AppointmentDAO;
import com.sunrisedental.model.AppointmentRequest;
import com.sunrisedental.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/patient/appointment-requests")
public class AppointmentRequestServlet extends HttpServlet {
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"status\":\"error\", \"message\":\"Unauthorized: Please log in.\"}");
            return;
        }

        User user = (User) session.getAttribute("user");

        String preferredDate = request.getParameter("preferredDate");
        String preferredTimeSlot = request.getParameter("preferredTimeSlot");
        String notes = request.getParameter("notes");
        String dentistIdStr = request.getParameter("preferredDentistId");
        String contactNo = request.getParameter("contactNo");
        String whatsappNo = request.getParameter("whatsappNo");

        if (preferredDate == null || preferredDate.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\": \"error\", \"message\": \"Preferred date is required.\"}");
            return;
        }

        Integer preferredDentistId = (dentistIdStr != null && !dentistIdStr.trim().isEmpty())
                ? Integer.parseInt(dentistIdStr) : null;

        AppointmentRequest appRequest = new AppointmentRequest();
        appRequest.setPatientUserId(user.getUserId());
        appRequest.setPatientCustomId(user.getCustomId());
        appRequest.setPreferredDate(preferredDate);
        appRequest.setPreferredTimeSlot(preferredTimeSlot);
        appRequest.setPreferredDentistId(preferredDentistId);
        appRequest.setNotes(notes);
        appRequest.setStatus("PENDING");
        appRequest.setContactNo(contactNo);
        appRequest.setWhatsappNo(whatsappNo);

        boolean isCreated = appointmentDAO.createAppointmentRequest(appRequest);

        if (isCreated) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("{\"status\": \"success\", \"message\": \"Appointment request submitted successfully! Pending Receptionist review.\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"status\": \"error\", \"message\": \"Failed to submit appointment request.\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"status\":\"error\", \"message\":\"Unauthorized: No active session.\"}");
            return;
        }

        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"status\":\"error\", \"message\":\"Unauthorized: User not logged in.\"}");
            return;
        }

        List<AppointmentRequest> requests = appointmentDAO.getRequestsByPatientId(user.getUserId());

        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\"status\": \"success\", \"data\": [");

        for (int i = 0; i < requests.size(); i++) {
            AppointmentRequest req = requests.get(i);

            jsonBuilder.append("{")
                    .append("\"requestId\":").append(req.getRequestId()).append(",")
                    .append("\"preferredDate\":\"").append(escapeJson(req.getPreferredDate())).append("\",")
                    .append("\"preferredTimeSlot\":\"").append(escapeJson(req.getPreferredTimeSlot())).append("\",")
                    .append("\"notes\":\"").append(escapeJson(req.getNotes())).append("\",")
                    .append("\"status\":\"").append(escapeJson(req.getStatus())).append("\",")
                    .append("\"contactNo\":\"").append(escapeJson(req.getContactNo())).append("\",")
                    .append("\"whatsappNo\":\"").append(escapeJson(req.getWhatsappNo())).append("\"")
                    .append("}");

            if (i < requests.size() - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]}");

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(jsonBuilder.toString());
    }

    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}