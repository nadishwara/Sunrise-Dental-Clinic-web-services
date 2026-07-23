package com.sunrisedental.dao;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.sunrisedental.config.DatabaseConnection;
import com.sunrisedental.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (username, email, password_hash, role) VALUES (?,?,?,?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getRole());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            System.err.println("Registration SQL Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public User authenticateUser(String email, String plainPassword) {
        String sql = "SELECT * FROM users WHERE email = ?";
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
                        user.setUsername(rs.getString("username"));
                        user.setEmail(rs.getString("email"));
                        user.setRole(rs.getString("role"));
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