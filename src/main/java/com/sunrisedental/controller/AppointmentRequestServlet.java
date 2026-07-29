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
        User user = (User) session.getAttribute("user");

        String preferredDate = request.getParameter("preferredDate");
        String preferredTimeSlot = request.getParameter("preferredTimeSlot");
        String notes = request.getParameter("notes");
        String dentistIdStr = request.getParameter("preferredDentistId");

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
            System.out.println("ERROR: Session is NULL!");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.getWriter().write("{\"status\":\"error\", \"message\":\"Unauthorized: No active session.\"}");
            return;
        }

        User user = (User) session.getAttribute("user");
        Integer userId = (Integer) session.getAttribute("userId");

        System.out.println("Session exists ID: " + session.getId());
        System.out.println("User in session: " + user);

        if (user == null && userId == null) {
            System.out.println("ERROR: User attribute is NOT set in session!");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"status\":\"error\", \"message\":\"Unauthorized: User not logged in.\"}");
            return;
        }
        User user1 = (User) session.getAttribute("user");
        System.out.println("SUCCESS: User logged in as ID: " + user.getUserId());

        List<AppointmentRequest> requests = appointmentDAO.getRequestsByPatientId(user.getUserId());
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\"status\": \"success\", \"data\": [");

        for (int i = 0; i< requests.size(); i++){
            AppointmentRequest req = requests.get(i);

            jsonBuilder.append("{")
                    .append("\"requestId\":").append(req.getRequestId()).append(",")
                    .append("\"preferredDate\":\"").append(req.getPreferredDate()).append("\",")
                    .append("\"preferredTimeSlot\":\"").append(req.getPreferredTimeSlot()).append("\",")
                    .append("\"notes\":\"").append(req.getNotes() != null ? req.getNotes().replace("\"", "\\\"") : "").append("\",")
                    .append("\"status\":\"").append(req.getStatus()).append("\"")
                    .append("}");
            if (i< requests.size()-1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]}");

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(jsonBuilder.toString());
    }
}
