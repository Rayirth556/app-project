package com.savora.dao;

import com.savora.model.Budget;
import com.savora.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Budget operations
 */
public class BudgetDAO {
    private final DatabaseConnection dbConnection;
    
    public BudgetDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Create a new budget
     */
    public boolean create(Budget budget) {
        String sql = "INSERT INTO budgets (category_id, amount, period, start_date, end_date, is_active) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, budget.getCategoryId());
            stmt.setBigDecimal(2, budget.getAmount());
            stmt.setString(3, budget.getPeriod());
            stmt.setDate(4, Date.valueOf(budget.getStartDate()));
            
            if (budget.getEndDate() != null) {
                stmt.setDate(5, Date.valueOf(budget.getEndDate()));
            } else {
                stmt.setNull(5, Types.DATE);
            }
            
            stmt.setBoolean(6, budget.isActive());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        budget.setBudgetId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating budget: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get budget by ID
     */
    public Budget findById(int budgetId) {
        String sql = "SELECT * FROM budgets WHERE budget_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, budgetId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBudget(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding budget by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get all budgets
     */
    public List<Budget> findAll() {
        String sql = "SELECT * FROM budgets ORDER BY start_date DESC";
        return executeQuery(sql);
    }
    
    /**
     * Get active budgets
     */
    public List<Budget> findActiveBudgets() {
        String sql = "SELECT * FROM budgets WHERE is_active = true ORDER BY start_date DESC";
        return executeQuery(sql);
    }
    
    /**
     * Get budgets by category
     */
    public List<Budget> findByCategory(int categoryId) {
        String sql = "SELECT * FROM budgets WHERE category_id = ? ORDER BY start_date DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                List<Budget> budgets = new ArrayList<>();
                while (rs.next()) {
                    budgets.add(mapResultSetToBudget(rs));
                }
                return budgets;
            }
        } catch (SQLException e) {
            System.err.println("Error finding budgets by category: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    /**
     * Get current active budget for a category
     */
    public Budget findCurrentBudgetForCategory(int categoryId) {
        String sql = "SELECT * FROM budgets WHERE category_id = ? AND is_active = true " +
                    "AND (end_date IS NULL OR end_date >= CURDATE()) ORDER BY start_date DESC LIMIT 1";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBudget(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding current budget for category: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get total spent amount for a category in current month
     */
    public BigDecimal getSpentAmountForCategory(int categoryId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
                    "WHERE category_id = ? AND type = 'expense' AND date BETWEEN ? AND ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting spent amount for category: " + e.getMessage());
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * Update budget
     */
    public boolean update(Budget budget) {
        String sql = "UPDATE budgets SET category_id = ?, amount = ?, period = ?, start_date = ?, end_date = ?, is_active = ? WHERE budget_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, budget.getCategoryId());
            stmt.setBigDecimal(2, budget.getAmount());
            stmt.setString(3, budget.getPeriod());
            stmt.setDate(4, Date.valueOf(budget.getStartDate()));
            
            if (budget.getEndDate() != null) {
                stmt.setDate(5, Date.valueOf(budget.getEndDate()));
            } else {
                stmt.setNull(5, Types.DATE);
            }
            
            stmt.setBoolean(6, budget.isActive());
            stmt.setInt(7, budget.getBudgetId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating budget: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Delete budget
     */
    public boolean delete(int budgetId) {
        String sql = "DELETE FROM budgets WHERE budget_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, budgetId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting budget: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Deactivate budget
     */
    public boolean deactivate(int budgetId) {
        String sql = "UPDATE budgets SET is_active = false WHERE budget_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, budgetId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deactivating budget: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Execute a query and return list of budgets
     */
    private List<Budget> executeQuery(String sql) {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            List<Budget> budgets = new ArrayList<>();
            while (rs.next()) {
                budgets.add(mapResultSetToBudget(rs));
            }
            return budgets;
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    /**
     * Map ResultSet to Budget object
     */
    private Budget mapResultSetToBudget(ResultSet rs) throws SQLException {
        Budget budget = new Budget();
        budget.setBudgetId(rs.getInt("budget_id"));
        budget.setCategoryId(rs.getInt("category_id"));
        budget.setAmount(rs.getBigDecimal("amount"));
        budget.setPeriod(rs.getString("period"));
        budget.setStartDate(rs.getDate("start_date").toLocalDate());
        
        Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            budget.setEndDate(endDate.toLocalDate());
        }
        
        budget.setActive(rs.getBoolean("is_active"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            budget.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return budget;
    }
}
