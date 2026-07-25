package com.sunrisedental.dao;


import com.sunrisedental.config.DatabaseConnection;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.AppointmentRequest;
import com.sunrisedental.util.IdGenerator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {
//    request appointment
    public boolean createAppointmentRequest(AppointmentRequest request) {
        String sql = "INSERT INTO appointment_requests (patient_user_id, patient_custom_id, preferred_date, preferred_time_slot, preferred_dentist_id, notes, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, request.getPatientUserId());
            stmt.setString(2, request.getPatientCustomId());
            stmt.setString(3, request.getPreferredDate());
            stmt.setString(4, request.getPreferredTimeSlot());

            if (request.getPreferredDentistId() != null) {
                stmt.setInt(5, request.getPreferredDentistId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.setString(6, request.getNotes());
            stmt.setString(7, request.getStatus()); // pending

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Create Appointment Request Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<AppointmentRequest> getRequestsByPatientId(int patientUserId) {
        List<AppointmentRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM appointment_requests WHERE patient_user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, patientUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AppointmentRequest req = new AppointmentRequest();
                    req.setRequestId(rs.getInt("request_id"));
                    req.setPatientUserId(rs.getInt("patient_user_id"));
                    req.setPatientCustomId(rs.getString("patient_custom_id"));
                    req.setPreferredDate(rs.getString("preferred_date"));
                    req.setPreferredTimeSlot(rs.getString("preferred_time_slot"));
                    req.setPreferredDentistId(rs.getObject("preferred_dentist_id") != null ? rs.getInt("preferred_dentist_id") : null);
                    req.setNotes(rs.getString("notes"));
                    req.setStatus(rs.getString("status"));
                    requests.add(req);
                }
            }
        } catch (SQLException e) {
            System.err.println("Get Patient Requests Error: " + e.getMessage());
            e.printStackTrace();
        }
        return requests;
    }

//    block double booking
    public boolean isSlotAvailable(int dentistId, String date, String time) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE dentist_id = ? AND appointment_date = ? AND appointment_time = ? AND status = 'SCHEDULED'";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, dentistId);
            stmt.setString(2, date);
            stmt.setString(3, time);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0; // Count value = 0 Slot should available.
                }
            }
        } catch (SQLException e) {
            System.err.println("Check Slot Availability Error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Transactional Appointment Booking execution (Appointments table Request status CONFIRMED)
    public String confirmAndBookAppointment(Appointment appointment) {
        String insertAppSql = "INSERT INTO appointments (custom_appointment_id, request_id, patient_id, dentist_id, receptionist_id, appointment_date, appointment_time, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String updateAppCustomIdSql = "UPDATE appointments SET custom_appointment_id = ? WHERE appointment_id = ?";
        String updateRequestSql = "UPDATE appointment_requests SET status = 'CONFIRMED' WHERE request_id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            // Insert appointments Record
            int generatedAppId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(insertAppSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, "TEMP");
                stmt.setInt(2, appointment.getRequestId());
                stmt.setInt(3, appointment.getPatientId());
                stmt.setInt(4, appointment.getDentistId());
                stmt.setInt(5, appointment.getReceptionistId());
                stmt.setString(6, appointment.getAppointmentDate());
                stmt.setString(7, appointment.getAppointmentTime());
                stmt.setString(8, "SCHEDULED");

                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedAppId = rs.getInt(1);
                    }
                }
            }

            if (generatedAppId == -1) {
                conn.rollback();
                return null;
            }

            String customAppId = IdGenerator.generateCustomId("APPOINTMENT", generatedAppId);
            try (PreparedStatement updateStmt = conn.prepareStatement(updateAppCustomIdSql)) {
                updateStmt.setString(1, customAppId);
                updateStmt.setInt(2, generatedAppId);
                updateStmt.executeUpdate();
            }

            // Step C: Update original request status to 'CONFIRMED'
            try (PreparedStatement updateReqStmt = conn.prepareStatement(updateRequestSql)) {
                updateReqStmt.setInt(1, appointment.getRequestId());
                updateReqStmt.executeUpdate();
            }

            conn.commit(); // Save all DB updates
            return customAppId;

        } catch (SQLException e) {
            System.err.println("Appointment Booking Transaction Error: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return null;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }
}