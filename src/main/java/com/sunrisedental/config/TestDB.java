package com.sunrisedental.config;

import java.sql.Connection;

public class TestDB {
    public static void main(String[] args) {
        System.out.println("Testing Database Connection...");

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("🎉 SUCCESS: Database Connected Successfully!");
            } else {
                System.out.println("🔴 FAILED: Connection object is null or closed.");
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR: Connection failed with exception!");
            e.printStackTrace();
        }
    }
}