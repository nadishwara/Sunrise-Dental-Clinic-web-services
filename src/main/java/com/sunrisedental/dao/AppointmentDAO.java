package com.sunrisedental.dao;

import com.sunrisedental.config.DatabaseConnection;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.AppointmentRequest;
import com.sunrisedental.util.IdGenerator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    public boolean createAppointmentRequest(AppointmentRequest request) {
        String updateUserSql = "UPDATE users SET contact_no = ?, whatsapp_no = ? WHERE user_id = ?";
        String insertReqSql = "INSERT INTO appointment_requests (patient_user_id, patient_custom_id, preferred_date, preferred_time_slot, preferred_dentist_id, notes, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement updateUserStmt = conn.prepareStatement(updateUserSql)) {
                updateUserStmt.setString(1, request.getContactNo());
                updateUserStmt.setString(2, request.getWhatsappNo());
                updateUserStmt.setInt(3, request.getPatientUserId());
                updateUserStmt.executeUpdate();
            }

            try (PreparedStatement insertReqStmt = conn.prepareStatement(insertReqSql)) {
                insertReqStmt.setInt(1, request.getPatientUserId());
                insertReqStmt.setString(2, request.getPatientCustomId());
                insertReqStmt.setString(3, request.getPreferredDate());
                insertReqStmt.setString(4, request.getPreferredTimeSlot());

                if (request.getPreferredDentistId() != null) {
                    insertReqStmt.setInt(5, request.getPreferredDentistId());
                } else {
                    insertReqStmt.setNull(5, Types.INTEGER);
                }

                insertReqStmt.setString(6, request.getNotes());
                insertReqStmt.setString(7, request.getStatus());

                insertReqStmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Create Appointment Request Error: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    public List<AppointmentRequest> getRequestsByPatientId(int patientUserId) {
        List<AppointmentRequest> requests = new ArrayList<>();
        String sql = "SELECT ar.request_id, ar.patient_user_id, ar.patient_custom_id, " +
                "u.username AS patient_name, u.email AS patient_email, u.contact_no, u.whatsapp_no, " +
                "ar.preferred_date, ar.preferred_time_slot, ar.preferred_dentist_id, ar.notes, ar.status, ar.created_at " +
                "FROM appointment_requests ar " +
                "LEFT JOIN users u ON ar.patient_user_id = u.user_id " +
                "WHERE ar.patient_user_id = ? ORDER BY ar.created_at DESC";

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

                    req.setPatientName(rs.getString("patient_name"));
                    req.setPatientEmail(rs.getString("patient_email"));

                    String phone = rs.getString("contact_no");
                    String whatsapp = rs.getString("whatsapp_no");
                    req.setContactNo(phone != null ? phone : "N/A");
                    req.setWhatsappNo(whatsapp != null ? whatsapp : "N/A");

                    requests.add(req);
                }
            }
        } catch (SQLException e) {
            System.err.println("Get Patient Requests Error: " + e.getMessage());
            e.printStackTrace();
        }
        return requests;
    }

    public boolean isSlotAvailable(int dentistId, String date, String time) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE dentist_id = ? AND appointment_date = ? AND appointment_time = ? AND status = 'SCHEDULED'";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, dentistId);
            stmt.setString(2, date);
            stmt.setString(3, time);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Check Slot Availability Error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public String confirmAndBookAppointment(Appointment appointment) {
        String insertAppSql = "INSERT INTO appointments (custom_appointment_id, request_id, patient_id, dentist_id, receptionist_id, appointment_date, appointment_time, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String updateAppCustomIdSql = "UPDATE appointments SET custom_appointment_id = ? WHERE appointment_id = ?";
        String updateRequestSql = "UPDATE appointment_requests SET status = 'CONFIRMED' WHERE request_id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

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

            try (PreparedStatement updateReqStmt = conn.prepareStatement(updateRequestSql)) {
                updateReqStmt.setInt(1, appointment.getRequestId());
                updateReqStmt.executeUpdate();
            }

            conn.commit();
            return customAppId;

        } catch (SQLException e) {
            System.err.println("Appointment Booking Transaction Error: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return null;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public List<AppointmentRequest> getAllAppointmentRequests() {
        List<AppointmentRequest> requests = new ArrayList<>();

        String sql = "SELECT ar.request_id, ar.patient_user_id, ar.patient_custom_id, u.username AS patient_name, u.email AS patient_email, u.contact_no, u.whatsapp_no,\n" +
                "           ar.preferred_date, ar.preferred_time_slot, ar.preferred_dentist_id, s.full_name AS dentist_name, ar.notes, ar.status, ar.created_at\n" +
                "    FROM appointment_requests ar\n" +
                "    LEFT JOIN users u ON ar.patient_user_id = u.user_id LEFT JOIN staff s ON ar.preferred_dentist_id = s.user_id ORDER BY ar.created_at DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                AppointmentRequest req = new AppointmentRequest();
                req.setRequestId(rs.getInt("request_id"));
                req.setPatientUserId(rs.getInt("patient_user_id"));
                req.setPatientCustomId(rs.getString("patient_custom_id"));
                req.setPreferredDate(rs.getString("preferred_date"));
                req.setPreferredTimeSlot(rs.getString("preferred_time_slot"));

                int dentistId = rs.getInt("preferred_dentist_id");
                if (rs.wasNull()) {
                    req.setPreferredDentistId(null);
                } else {
                    req.setPreferredDentistId(dentistId);
                }

                req.setNotes(rs.getString("notes"));
                req.setStatus(rs.getString("status"));
                req.setPatientName(rs.getString("patient_name"));
                req.setPatientEmail(rs.getString("patient_email"));

                String phone = rs.getString("contact_no");
                String whatsapp = rs.getString("whatsapp_no");

                req.setContactNo(phone != null ? phone : "N/A"); // Fixed duplicate
                req.setWhatsappNo(whatsapp != null ? whatsapp : "N/A");

                requests.add(req);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return requests;
    }

    public boolean updateAppointmentRequestStatus(int requestId, String status) {
        String sql = "UPDATE appointment_requests SET status = ? WHERE request_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, requestId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}