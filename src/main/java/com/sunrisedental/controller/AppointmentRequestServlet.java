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
        User user = (User) session.getAttribute("user");

        List<AppointmentRequest> requests = appointmentDAO.getRequestsByPatientId(user.getUserId());

        response.setStatus(HttpServletResponse.SC_OK);
        // use JSON Parsing library
        response.getWriter().write(String.format("{\"status\": \"success\", \"count\": %d}", requests.size()));
    }
}
