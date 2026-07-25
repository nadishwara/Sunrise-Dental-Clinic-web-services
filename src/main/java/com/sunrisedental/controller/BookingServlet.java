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

@WebServlet("/api/reception/appointments/book")
public class BookingServlet extends HttpServlet {
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        User receptionist = (User) session.getAttribute("user");

        String requestIdStr = request.getParameter("requestId");
        String patientIdStr = request.getParameter("patientId");
        String dentistIdStr = request.getParameter("dentistId");
        String appointmentDate = request.getParameter("appointmentDate");
        String appointmentTime = request.getParameter("appointmentTime");

        if (patientIdStr == null || dentistIdStr == null || appointmentDate == null || appointmentTime == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\": \"error\", \"message\": \"Missing required booking parameters.\"}");
            return;
        }

        int requestId = Integer.parseInt(requestIdStr);
        int patientId = Integer.parseInt(patientIdStr);
        int dentistId = Integer.parseInt(dentistIdStr);

        // Doctor Availability Slot Check
        boolean isSlotAvailable = appointmentDAO.isSlotAvailable(dentistId, appointmentDate, appointmentTime);
        if (!isSlotAvailable) {
            response.setStatus(HttpServletResponse.SC_CONFLICT); // HTTP 409
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

        // Transactional Booking Execution
        String customAppId = appointmentDAO.confirmAndBookAppointment(appointment);

        if (customAppId != null) {
            response.setStatus(HttpServletResponse.SC_CREATED);

            // generate whatsapp link URL)
            String whatsappMsg = String.format("Hello! Your Sunrise Dental Appointment (%s) is CONFIRMED for %s at %s.",
                    customAppId, appointmentDate, appointmentTime);

            response.getWriter().write(String.format(
                    "{\"status\": \"success\", \"message\": \"Appointment booked successfully!\", \"customId\": \"%s\", \"whatsappMessage\": \"%s\"}",
                    customAppId, whatsappMsg
            ));
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"status\": \"error\", \"message\": \"Failed to complete appointment booking.\"}");
        }
    }
}
