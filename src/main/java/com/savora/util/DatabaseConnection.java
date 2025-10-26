package com.savora.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Utility class for managing database connections
 */
public class DatabaseConnection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/savora_finance";
    private static final String DB_USER = "root"; // Using default root user
    private static final String DB_PASSWORD = "whatthe";   // Using default root password
    
    private static DatabaseConnection instance;
    private Connection connection;
    
    private DatabaseConnection() {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Set connection properties
            Properties props = new Properties();
            props.setProperty("user", DB_USER);
            props.setProperty("password", DB_PASSWORD);
            props.setProperty("serverTimezone", "UTC");
            
            // First try to create the database if it doesn't exist
            try (Connection tempConn = DriverManager.getConnection("jdbc:mysql://localhost:3306", props)) {
                Statement stmt = tempConn.createStatement();
                stmt.execute("CREATE DATABASE IF NOT EXISTS savora_finance");
            }
            
            connection = DriverManager.getConnection(DB_URL, props);
            System.out.println("Database connection established successfully.");
            
            // Initialize database schema
            initializeDatabase();
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Failed to establish database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get singleton instance of DatabaseConnection
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    /**
     * Get database connection
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Reconnect if connection is closed
                Properties props = new Properties();
                props.setProperty("user", DB_USER);
                props.setProperty("password", DB_PASSWORD);
                
                connection = DriverManager.getConnection(DB_URL, props);
            }
        } catch (SQLException e) {
            System.err.println("Failed to get database connection: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }
    
    /**
     * Close database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Failed to close database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Initialize database schema
     */
    private void initializeDatabase() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream("/schema.sql")))) {
            
            StringBuilder sql = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip comments and empty lines
                if (line.trim().isEmpty() || line.trim().startsWith("--")) {
                    continue;
                }
                sql.append(line).append("\n");
            }
            
            // Split SQL statements and execute them
            String[] statements = sql.toString().split(";");
            try (Statement stmt = connection.createStatement()) {
                for (String statement : statements) {
                    statement = statement.trim();
                    if (!statement.isEmpty()) {
                        try {
                            stmt.execute(statement);
                        } catch (SQLException se) {
                            // Ignore duplicate-key / already-exists errors while initializing schema
                            String msg = se.getMessage() != null ? se.getMessage().toLowerCase() : "";
                            if (msg.contains("duplicate key") || msg.contains("already exists") || msg.contains("duplicate key name") || msg.contains("errno 1061")) {
                                // benign when schema was previously initialized
                                System.out.println("Schema statement skipped: " + se.getMessage());
                                continue;
                            }
                            // rethrow if it's a different SQL error
                            throw se;
                        }
                    }
                }
            }
            
            System.out.println("Database schema initialized successfully.");
        } catch (Exception e) {
            System.err.println("Failed to initialize database schema: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test database connection
     */
    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}
