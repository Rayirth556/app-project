package com.savora.dao;

import com.savora.model.SpendingLimit;
import com.savora.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for SpendingLimit operations
 */
public class SpendingLimitDAO {
    private final DatabaseConnection dbConnection;
    
    public SpendingLimitDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Create a new spending limit
     */
    public boolean create(SpendingLimit spendingLimit) {
        String sql = "INSERT INTO spending_limits (category_id, limit_amount, period, is_active) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            if (spendingLimit.getCategoryId() != null) {
                stmt.setInt(1, spendingLimit.getCategoryId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            
            stmt.setBigDecimal(2, spendingLimit.getLimitAmount());
            stmt.setString(3, spendingLimit.getPeriod());
            stmt.setBoolean(4, spendingLimit.isActive());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        spendingLimit.setLimitId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating spending limit: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get spending limit by ID
     */
    public SpendingLimit findById(int limitId) {
        String sql = "SELECT * FROM spending_limits WHERE limit_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limitId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSpendingLimit(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding spending limit by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get all spending limits
     */
    public List<SpendingLimit> findAll() {
        String sql = "SELECT * FROM spending_limits ORDER BY category_id, limit_amount";
        return executeQuery(sql);
    }
    
    /**
     * Get active spending limits
     */
    public List<SpendingLimit> findActiveLimits() {
        String sql = "SELECT * FROM spending_limits WHERE is_active = true ORDER BY category_id, limit_amount";
        return executeQuery(sql);
    }
    
    /**
     * Get spending limit by category
     */
    public SpendingLimit findByCategory(int categoryId) {
        String sql = "SELECT * FROM spending_limits WHERE category_id = ? AND is_active = true ORDER BY limit_amount LIMIT 1";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSpendingLimit(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding spending limit by category: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get overall spending limit (category_id is null)
     */
    public SpendingLimit findOverallLimit() {
        String sql = "SELECT * FROM spending_limits WHERE category_id IS NULL AND is_active = true ORDER BY limit_amount LIMIT 1";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return mapResultSetToSpendingLimit(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding overall spending limit: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get spending limits by period
     */
    public List<SpendingLimit> findByPeriod(String period) {
        String sql = "SELECT * FROM spending_limits WHERE period = ? AND is_active = true ORDER BY category_id, limit_amount";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, period);
            
            try (ResultSet rs = stmt.executeQuery()) {
                List<SpendingLimit> limits = new ArrayList<>();
                while (rs.next()) {
                    limits.add(mapResultSetToSpendingLimit(rs));
                }
                return limits;
            }
        } catch (SQLException e) {
            System.err.println("Error finding spending limits by period: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    /**
     * Update spending limit
     */
    public boolean update(SpendingLimit spendingLimit) {
        String sql = "UPDATE spending_limits SET category_id = ?, limit_amount = ?, period = ?, is_active = ? WHERE limit_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (spendingLimit.getCategoryId() != null) {
                stmt.setInt(1, spendingLimit.getCategoryId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            
            stmt.setBigDecimal(2, spendingLimit.getLimitAmount());
            stmt.setString(3, spendingLimit.getPeriod());
            stmt.setBoolean(4, spendingLimit.isActive());
            stmt.setInt(5, spendingLimit.getLimitId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating spending limit: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Delete spending limit
     */
    public boolean delete(int limitId) {
        String sql = "DELETE FROM spending_limits WHERE limit_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limitId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting spending limit: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Deactivate spending limit
     */
    public boolean deactivate(int limitId) {
        String sql = "UPDATE spending_limits SET is_active = false WHERE limit_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limitId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deactivating spending limit: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Check if spending limit exists for category
     */
    public boolean existsForCategory(int categoryId) {
        String sql = "SELECT COUNT(*) FROM spending_limits WHERE category_id = ? AND is_active = true";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking spending limit existence: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Check if overall spending limit exists
     */
    public boolean existsOverallLimit() {
        String sql = "SELECT COUNT(*) FROM spending_limits WHERE category_id IS NULL AND is_active = true";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error checking overall spending limit existence: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Execute a query and return list of spending limits
     */
    private List<SpendingLimit> executeQuery(String sql) {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            List<SpendingLimit> limits = new ArrayList<>();
            while (rs.next()) {
                limits.add(mapResultSetToSpendingLimit(rs));
            }
            return limits;
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    /**
     * Map ResultSet to SpendingLimit object
     */
    private SpendingLimit mapResultSetToSpendingLimit(ResultSet rs) throws SQLException {
        SpendingLimit spendingLimit = new SpendingLimit();
        spendingLimit.setLimitId(rs.getInt("limit_id"));
        
        int categoryId = rs.getInt("category_id");
        if (!rs.wasNull()) {
            spendingLimit.setCategoryId(categoryId);
        }
        
        spendingLimit.setLimitAmount(rs.getBigDecimal("limit_amount"));
        spendingLimit.setPeriod(rs.getString("period"));
        spendingLimit.setActive(rs.getBoolean("is_active"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            spendingLimit.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return spendingLimit;
    }
}
