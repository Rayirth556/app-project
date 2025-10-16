package com.savora.dao;

import com.savora.model.Category;
import com.savora.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Category operations
 */
public class CategoryDAO {
    private final DatabaseConnection dbConnection;
    
    public CategoryDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Create a new category
     */
    public boolean create(Category category) {
        String sql = "INSERT INTO categories (name, is_default) VALUES (?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, category.getName());
            stmt.setBoolean(2, category.isDefault());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        category.setCategoryId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating category: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get category by ID
     */
    public Category findById(int categoryId) {
        String sql = "SELECT * FROM categories WHERE category_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCategory(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding category by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get category by name
     */
    public Category findByName(String name) {
        String sql = "SELECT * FROM categories WHERE name = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCategory(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding category by name: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get all categories
     */
    public List<Category> findAll() {
        String sql = "SELECT * FROM categories ORDER BY name";
        return executeQuery(sql);
    }
    
    /**
     * Get default categories
     */
    public List<Category> findDefaultCategories() {
        String sql = "SELECT * FROM categories WHERE is_default = true ORDER BY name";
        return executeQuery(sql);
    }
    
    /**
     * Get custom categories
     */
    public List<Category> findCustomCategories() {
        String sql = "SELECT * FROM categories WHERE is_default = false ORDER BY name";
        return executeQuery(sql);
    }
    
    /**
     * Update category
     */
    public boolean update(Category category) {
        String sql = "UPDATE categories SET name = ?, is_default = ? WHERE category_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, category.getName());
            stmt.setBoolean(2, category.isDefault());
            stmt.setInt(3, category.getCategoryId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating category: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Delete category (only if not default and no transactions exist)
     */
    public boolean delete(int categoryId) {
        // Check if category is default
        Category category = findById(categoryId);
        if (category != null && category.isDefault()) {
            System.err.println("Cannot delete default category: " + category.getName());
            return false;
        }
        
        // Check if category has transactions
        String checkSql = "SELECT COUNT(*) FROM transactions WHERE category_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            
            stmt.setInt(1, categoryId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.err.println("Cannot delete category with existing transactions");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking category usage: " + e.getMessage());
            return false;
        }
        
        // Delete category
        String sql = "DELETE FROM categories WHERE category_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting category: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Check if category exists by name
     */
    public boolean existsByName(String name) {
        String sql = "SELECT COUNT(*) FROM categories WHERE name = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, name);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking category existence: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Execute a query and return list of categories
     */
    private List<Category> executeQuery(String sql) {
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            List<Category> categories = new ArrayList<>();
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
            return categories;
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    /**
     * Map ResultSet to Category object
     */
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setCategoryId(rs.getInt("category_id"));
        category.setName(rs.getString("name"));
        category.setDefault(rs.getBoolean("is_default"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            category.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return category;
    }
}
