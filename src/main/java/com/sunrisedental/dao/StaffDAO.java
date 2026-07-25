package com.sunrisedental.dao;

import com.sunrisedental.config.DatabaseConnection;
import com.sunrisedental.model.Staff;
import com.sunrisedental.model.User;
import com.sunrisedental.util.IdGenerator;

import javax.xml.transform.Result;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;


public class StaffDAO {
    public boolean registerStaff(User user, Staff staff) {
        String insertUserSql = "INSERT INTO users (username, email, password_hash, role, custom_id) VALUES (?, ?, ?, ?, ?)";
        String updateCustomIdSql = "UPDATE users SET custom_id = ? WHERE user_id = ?";
        String insertStaffSql = "INSERT INTO staff (user_id, custom_staff_id, full_name, contact_no, specialization) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            int generatedUserId = -1;
            String tempCustomId = "TEMP-" + UUID.randomUUID().toString().substring(0, 8);
            try (PreparedStatement userStmt = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, user.getUsername());
                userStmt.setString(2, user.getEmail());
                userStmt.setString(3, user.getPassword());
                userStmt.setString(4, user.getRole());
                userStmt.setString(5, tempCustomId);

                int affectedStaff = userStmt.executeUpdate();
                if (affectedStaff == 0) {
                    conn.rollback();
                    return false;
                }
                try (ResultSet generatedKeys = userStmt.getGeneratedKeys()){
                    if (generatedKeys.next()) {
                        generatedUserId = generatedKeys.getInt(1);
                    } else {
                        conn.rollback();;
                        return false;
                    }
                }
            }
            String customId = IdGenerator.generateCustomId(user.getRole(), generatedUserId);
            try (PreparedStatement updateStmt = conn.prepareStatement(updateCustomIdSql)){
                updateStmt.setString(1, customId);
                updateStmt.setInt(2, generatedUserId);
                updateStmt.executeUpdate();
            }
            try (PreparedStatement staffStmt = conn.prepareStatement(insertStaffSql)){
                staffStmt.setInt(1, generatedUserId);
                staffStmt.setString(2, customId);
                staffStmt.setString(3, staff.getFullName());
                staffStmt.setString(4, staff.getContactNo());
                staffStmt.setString(5, staff.getSpecialization());

                staffStmt.executeUpdate();
            }

            conn.commit(); // Commit Transaction
            user.setCustomId(customId);
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.out.println("Staff Register SQL Error: "+e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}