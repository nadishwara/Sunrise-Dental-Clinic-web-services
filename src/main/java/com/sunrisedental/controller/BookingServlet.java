package com.sunrisedental.controller;

import com.sunrisedental.dao.AppointmentDAO;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/api/reception/appointments/book")
public class BookingServlet extends HttpServlet {
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"status\": \"error\", \"message\": \"Unauthorized. Please login as Receptionist.\"}");
            return;
        }

        User receptionist = (User) session.getAttribute("user");

        String requestIdStr = request.getParameter("requestId");
        String patientIdStr = request.getParameter("patientId");
        String dentistIdStr = request.getParameter("dentistId");
        String appointmentDate = request.getParameter("appointmentDate");
        String appointmentTime = request.getParameter("appointmentTime");

        if (patientIdStr == null || patientIdStr.trim().isEmpty() ||
                dentistIdStr == null || dentistIdStr.trim().isEmpty() ||
                appointmentDate == null || appointmentDate.trim().isEmpty() ||
                appointmentTime == null || appointmentTime.trim().isEmpty()) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\": \"error\", \"message\": \"Missing required booking parameters.\"}");
            return;
        }

        try {
            int patientId = Integer.parseInt(patientIdStr);
            int dentistId = Integer.parseInt(dentistIdStr);
            Integer requestId = (requestIdStr != null && !requestIdStr.trim().isEmpty()) ? Integer.parseInt(requestIdStr) : null;

            // 3. Slot Availability Check
            boolean isSlotAvailable = appointmentDAO.isSlotAvailable(dentistId, appointmentDate, appointmentTime);
            if (!isSlotAvailable) {
                response.setStatus(HttpServletResponse.SC_CONFLICT); // 409 Conflict
                response.getWriter().write("{\"status\": \"error\", \"message\": \"Doctor is already booked for this time slot.\"}");
                return;
            }

            Appointment appointment = new Appointment();
            appointment.setRequestId(requestId);
            appointment.setPatientId(patientId);
            appointment.setDentistId(dentistId);
            appointment.setReceptionistId(receptionist.getUserId());
            appointment.setAppointmentDate(appointmentDate);
            appointment.setAppointmentTime(appointmentTime);
            appointment.setStatus("SCHEDULED");

            // 4. Save to DB and Generate Custom ID (APP-001) Transactionally
            String customAppId = appointmentDAO.confirmAndBookAppointment(appointment);

            if (customAppId != null) {
                response.setStatus(HttpServletResponse.SC_CREATED);

                String rawMsg = String.format("Hello! Your Sunrise Dental Appointment (%s) is CONFIRMED for %s at %s.",
                        customAppId, appointmentDate, appointmentTime);

                String encodedMsg = URLEncoder.encode(rawMsg, StandardCharsets.UTF_8.toString());

                response.getWriter().write(String.format(
                        "{\"status\": \"success\", \"message\": \"Appointment booked successfully!\", \"customId\": \"%s\", \"whatsappMessage\": \"%s\", \"encodedWhatsappMsg\": \"%s\"}",
                        customAppId, rawMsg.replace("\"", "\\\""), encodedMsg
                ));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"status\": \"error\", \"message\": \"Failed to complete appointment booking.\"}");
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\": \"error\", \"message\": \"Invalid numeric values for IDs.\"}");
        }
    }
}