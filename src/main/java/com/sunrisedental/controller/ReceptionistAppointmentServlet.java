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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/api/receptionist/appointment-requests/*")
public class ReceptionistAppointmentServlet extends HttpServlet {
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("\n[FETCH REQUESTS] Fetching all appointment requests...");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null) {
            System.err.println("[AUTH ERROR] Session expired or user is not logged in.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"status\":\"error\", \"message\":\"Unauthorized: Session expired or invalid.\"}");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (user == null || (!"RECEPTIONIST".equalsIgnoreCase(user.getRole()) && !"ADMIN".equalsIgnoreCase(user.getRole()))) {
            System.err.println("[ACCESS DENIED] User does not have receptionist or admin permissions.");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"status\":\"error\", \"message\":\"Forbidden: Access denied.\"}");
            return;
        }

        List<AppointmentRequest> requests = appointmentDAO.getAllAppointmentRequests();
        System.out.println("[SUCCESS] Successfully retrieved " + requests.size() + " appointment request(s).");

        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\"status\": \"success\", \"data\": [");

        for (int i = 0; i < requests.size(); i++) {
            AppointmentRequest req = requests.get(i);
            jsonBuilder.append("{")
                    .append("\"requestId\":").append(req.getRequestId()).append(",")
                    .append("\"patientUserId\":").append(req.getPatientUserId()).append(",")
                    .append("\"patientCustomId\":\"").append(escapeJson(req.getPatientCustomId())).append("\",")
                    .append("\"patientName\":\"").append(escapeJson(req.getPatientName())).append("\",")
                    .append("\"patientEmail\":\"").append(escapeJson(req.getPatientEmail())).append("\",")
                    .append("\"patientPhone\":\"").append(escapeJson(req.getContactNo() != null ? req.getContactNo() : "N/A")).append("\",")
                    .append("\"contactNo\":\"").append(escapeJson(req.getContactNo())).append("\",")
                    .append("\"whatsappNo\":\"").append(escapeJson(req.getWhatsappNo())).append("\",")
                    .append("\"preferredDate\":\"").append(escapeJson(req.getPreferredDate())).append("\",")
                    .append("\"preferredTimeSlot\":\"").append(escapeJson(req.getPreferredTimeSlot())).append("\",")
                    .append("\"preferredDentistId\":").append(req.getPreferredDentistId()).append(",")
                    .append("\"notes\":\"").append(escapeJson(req.getNotes())).append("\",")
                    .append("\"status\":\"").append(escapeJson(req.getStatus())).append("\"")
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

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("\n[UPDATE REQUEST] Processing appointment status update...");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null) {
            System.err.println("[AUTH ERROR] Session expired or invalid.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"status\":\"error\", \"message\":\"Unauthorized: Session expired\"}");
            return;
        }

        User user = (User) session.getAttribute("user");
        if (user == null || (!"RECEPTIONIST".equalsIgnoreCase(user.getRole()) && !"ADMIN".equalsIgnoreCase(user.getRole()))) {
            System.err.println("[ACCESS DENIED] User is not authorized to update status.");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"status\":\"error\", \"message\":\"Forbidden access\"}");
            return;
        }

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            System.err.println("[INVALID URL] Missing appointment request ID in URL.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\":\"error\", \"message\":\"Invalid request path.\"}");
            return;
        }

        String[] splits = pathInfo.split("/");
        if (splits.length < 3 || !"status".equalsIgnoreCase(splits[2])) {
            System.err.println("[INVALID URL] URL format is incorrect. Expected: /<id>/status");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\":\"error\", \"message\":\"Endpoint format should be /{id}/status\"}");
            return;
        }

        try {
            int requestId = Integer.parseInt(splits[1]);

            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            String body = buffer.toString();

            String newStatus = null;
            if (body != null) {
                Pattern pattern = Pattern.compile("\"status\"\\s*:\\s*\"([^\"]+)\"");
                Matcher matcher = pattern.matcher(body);
                if (matcher.find()) {
                    newStatus = matcher.group(1).trim();
                }
            }

            if (newStatus != null && !newStatus.isEmpty()) {
                System.out.println(" --> Updating Request ID: " + requestId + " -> New Status: " + newStatus);

                boolean updated = appointmentDAO.updateAppointmentRequestStatus(requestId, newStatus);

                if (updated) {
                    System.out.println("[SUCCESS] Appointment Request #" + requestId + " updated to " + newStatus);
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("{\"status\":\"success\", \"message\":\"Status updated successfully.\"}");
                } else {
                    System.err.println("[DATABASE ERROR] Could not update Request #" + requestId + " in the database.");
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("{\"status\":\"error\", \"message\":\"Failed to update status in DB.\"}");
                }
            } else {
                System.err.println("[PAYLOAD ERROR] Provided JSON body does not contain a valid status value.");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"status\":\"error\", \"message\":\"Invalid status payload.\"}");
            }

        } catch (NumberFormatException e) {
            System.err.println("[INVALID ID] Request ID in URL is not a valid integer.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\":\"error\", \"message\":\"Invalid Request ID format.\"}");
        } catch (Exception e) {
            System.err.println("[SERVER ERROR] Unexpected error while updating status: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"status\":\"error\", \"message\":\"Server error occurred.\"}");
        }
    }
}