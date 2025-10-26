package com.savora.dao;

import com.savora.model.StockSymbol;
import com.savora.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for StockSymbol operations
 */
public class SymbolDAO {
    private final DatabaseConnection dbConnection;
    
    public SymbolDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Get all stock symbols
     */
    public List<StockSymbol> findAll() {
        String sql = "SELECT * FROM stock_symbols ORDER BY symbol";
        List<StockSymbol> symbols = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                symbols.add(mapResultSetToSymbol(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all symbols: " + e.getMessage());
            e.printStackTrace();
        }
        return symbols;
    }
    
    /**
     * Find symbol by ID
     */
    public StockSymbol findById(int symbolId) {
        String sql = "SELECT * FROM stock_symbols WHERE symbol_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, symbolId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSymbol(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding symbol by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Find symbol by ticker symbol
     */
    public StockSymbol findBySymbol(String symbol) {
        String sql = "SELECT * FROM stock_symbols WHERE symbol = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, symbol);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSymbol(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding symbol: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Create new symbol
     */
    public boolean create(StockSymbol stockSymbol) {
        String sql = "INSERT INTO stock_symbols (symbol, name) VALUES (?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, stockSymbol.getSymbol());
            stmt.setString(2, stockSymbol.getName());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        stockSymbol.setSymbolId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating symbol: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Map ResultSet to StockSymbol
     */
    private StockSymbol mapResultSetToSymbol(ResultSet rs) throws SQLException {
        StockSymbol symbol = new StockSymbol();
        symbol.setSymbolId(rs.getInt("symbol_id"));
        symbol.setSymbol(rs.getString("symbol"));
        symbol.setName(rs.getString("name"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            symbol.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return symbol;
    }
}
