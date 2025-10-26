package com.savora.dao;

import com.savora.model.SimulatedAccount;
import com.savora.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for SimulatedAccount operations
 */
public class AccountDAO {
    private final DatabaseConnection dbConnection;
    
    public AccountDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Get account by ID
     */
    public SimulatedAccount findById(int accountId) {
        String sql = "SELECT * FROM simulated_accounts WHERE account_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, accountId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAccount(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding account: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get all accounts
     */
    public List<SimulatedAccount> findAll() {
        String sql = "SELECT * FROM simulated_accounts ORDER BY account_id";
        List<SimulatedAccount> accounts = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all accounts: " + e.getMessage());
            e.printStackTrace();
        }
        return accounts;
    }
    
    /**
     * Create new account
     */
    public boolean create(SimulatedAccount account) {
        String sql = "INSERT INTO simulated_accounts (account_name, initial_cash, current_cash) VALUES (?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, account.getAccountName());
            stmt.setBigDecimal(2, account.getInitialCash());
            stmt.setBigDecimal(3, account.getCurrentCash());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        account.setAccountId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating account: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Update account cash balance
     */
    public boolean updateCash(int accountId, java.math.BigDecimal newCash) {
        String sql = "UPDATE simulated_accounts SET current_cash = ? WHERE account_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, newCash);
            stmt.setInt(2, accountId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating account cash: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Map ResultSet to SimulatedAccount
     */
    private SimulatedAccount mapResultSetToAccount(ResultSet rs) throws SQLException {
        SimulatedAccount account = new SimulatedAccount();
        account.setAccountId(rs.getInt("account_id"));
        account.setAccountName(rs.getString("account_name"));
        account.setInitialCash(rs.getBigDecimal("initial_cash"));
        account.setCurrentCash(rs.getBigDecimal("current_cash"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            account.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            account.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return account;
    }
}
