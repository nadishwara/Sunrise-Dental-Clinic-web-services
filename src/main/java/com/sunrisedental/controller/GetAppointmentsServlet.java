package com.sunrisedental.controller;

import com.google.gson.Gson;
import com.sunrisedental.dao.AppointmentDAO;
import com.sunrisedental.model.Appointment;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/reception/appointments")
public class GetAppointmentsServlet extends HttpServlet {
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session==null || session.getAttribute("user")== null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"status\": \"error\", \"message\": \"Unauthorized access.\"}");
            return;
        }
        try {
            List<Appointment> appointments = appointmentDAO.getAllAppointmentData();
            String jsonOutput = this.gson.toJson(appointments);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(jsonOutput);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"status\": \"error\", \"message\": \"Failed to retrieve appointments.\"}");
        }
    }

}
