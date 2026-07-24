package com.sunrisedental.dao;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.sunrisedental.config.DatabaseConnection;
import com.sunrisedental.model.User;
import com.sunrisedental.util.IdGenerator;

import java.sql.*;
import java.util.UUID;

public class UserDAO {

    public boolean registerUser(User user) {
        String insertSql = "INSERT INTO users (username, email, password_hash, role, custom_id) VALUES (?,?,?,?,?)";
        String updateCustomIdSql = "UPDATE users SET custom_id = ? WHERE user_id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Start Transaction

            int generatedUserId = -1;
            String tempCustomId = "TEMP-" + UUID.randomUUID().toString().substring(0, 8);

            //Insert temporary custom_id
            try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getEmail());
                stmt.setString(3, user.getPassword());
                stmt.setString(4, user.getRole());
                stmt.setString(5, tempCustomId);

                int rows = stmt.executeUpdate();
                if (rows == 0) {
                    conn.rollback();
                    return false;
                }

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedUserId = rs.getInt(1);
                    }
                }
            }

            if (generatedUserId == -1) {
                conn.rollback();
                return false;
            }

            // Generate final Custom ID (e.g., DEN001, PTN002)
            String customId = IdGenerator.generateCustomId(user.getRole(), generatedUserId);

            // Update with formatted Custom ID
            try (PreparedStatement updateStmt = conn.prepareStatement(updateCustomIdSql)) {
                updateStmt.setString(1, customId);
                updateStmt.setInt(2, generatedUserId);
                updateStmt.executeUpdate();
            }

            conn.commit();

            user.setUserId(generatedUserId);
            user.setCustomId(customId);
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Registration SQL Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close(); // Return connection to pool
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public User authenticateUser(String email, String plainPassword) {
        String sql = "SELECT u.*, s.staff_id, s.full_name FROM users u " +
                "LEFT JOIN staff s ON u.user_id = s.user_id " +
                "WHERE u.email = ?";
        User user = null;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHashedPassword = rs.getString("password_hash");
                    BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), storedHashedPassword);

                    if (result.verified) {
                        user = new User();
                        user.setUserId(rs.getInt("user_id"));
                        user.setCustomId(rs.getString("custom_id"));
                        user.setUsername(rs.getString("username"));
                        user.setEmail(rs.getString("email"));
                        user.setRole(rs.getString("role"));

                        int staffId = rs.getInt("staff_id");
                        if (!rs.wasNull()) {
                            user.setStaffId(staffId);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Authentication SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
        return user;
    }
}