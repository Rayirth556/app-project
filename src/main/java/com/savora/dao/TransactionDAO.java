package com.savora.dao;

import com.savora.model.Transaction;
import com.savora.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Transaction operations
 */
public class TransactionDAO {
    private final DatabaseConnection dbConnection;
    
    public TransactionDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Create a new transaction
     */
    public boolean create(Transaction transaction) {
        String sql = "INSERT INTO transactions (amount, date, category_id, description, type) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setBigDecimal(1, transaction.getAmount());
            stmt.setDate(2, Date.valueOf(transaction.getDate()));
            stmt.setInt(3, transaction.getCategoryId());
            stmt.setString(4, transaction.getDescription());
            stmt.setString(5, transaction.getType().getValue());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        transaction.setTransactionId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating transaction: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get transaction by ID
     */
    public Transaction findById(int transactionId) {
        String sql = "SELECT * FROM transactions WHERE transaction_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, transactionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTransaction(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding transaction by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get all transactions
     */
    public List<Transaction> findAll() {
        String sql = "SELECT * FROM transactions ORDER BY date DESC, transaction_id DESC";
        return executeQuery(sql);
    }
    
    /**
     * Get transactions by date range
     */
    public List<Transaction> findByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM transactions WHERE date BETWEEN ? AND ? ORDER BY date DESC, transaction_id DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                List<Transaction> transactions = new ArrayList<>();
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
                return transactions;
            }
        } catch (SQLException e) {
            System.err.println("Error finding transactions by date range: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    /**
     * Get transactions by category
     */
    public List<Transaction> findByCategory(int categoryId) {
        String sql = "SELECT * FROM transactions WHERE category_id = ? ORDER BY date DESC, transaction_id DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                List<Transaction> transactions = new ArrayList<>();
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
                return transactions;
            }
        } catch (SQLException e) {
            System.err.println("Error finding transactions by category: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    /**
     * Get transactions by type (income/expense)
     */
    public List<Transaction> findByType(Transaction.TransactionType type) {
        String sql = "SELECT * FROM transactions WHERE type = ? ORDER BY date DESC, transaction_id DESC";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, type.getValue());
            
            try (ResultSet rs = stmt.executeQuery()) {
                List<Transaction> transactions = new ArrayList<>();
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
                return transactions;
            }
        } catch (SQLException e) {
            System.err.println("Error finding transactions by type: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    /**
     * Update transaction
     */
    public boolean update(Transaction transaction) {
        String sql = "UPDATE transactions SET amount = ?, date = ?, category_id = ?, description = ?, type = ? WHERE transaction_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, transaction.getAmount());
            stmt.setDate(2, Date.valueOf(transaction.getDate()));
            stmt.setInt(3, transaction.getCategoryId());
            stmt.setString(4, transaction.getDescription());
            stmt.setString(5, transaction.getType().getValue());
            stmt.setInt(6, transaction.getTransactionId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating transaction: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Delete transaction
     */
    public boolean delete(int transactionId) {
        String sql = "DELETE FROM transactions WHERE transaction_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, transactionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get total income for a date range
     */
    public BigDecimal getTotalIncome(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'income' AND date BETWEEN ? AND ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting total income: " + e.getMessage());
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * Get total expenses for a date range
     */
    public BigDecimal getTotalExpenses(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'expense' AND date BETWEEN ? AND ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting total expenses: " + e.getMessage());
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * Execute a query and return list of transactions
     */
    private List<Transaction> executeQuery(String sql) {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            List<Transaction> transactions = new ArrayList<>();
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
            return transactions;
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    /**
     * Map ResultSet to Transaction object
     */
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(rs.getInt("transaction_id"));
        transaction.setAmount(rs.getBigDecimal("amount"));
        transaction.setDate(rs.getDate("date").toLocalDate());
        transaction.setCategoryId(rs.getInt("category_id"));
        transaction.setDescription(rs.getString("description"));
        transaction.setType(Transaction.TransactionType.fromString(rs.getString("type")));
        return transaction;
    }
}
