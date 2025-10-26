package com.savora.dao;

import com.savora.model.Position;
import com.savora.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Position operations
 */
public class PositionDAO {
    private final DatabaseConnection dbConnection;
    
    public PositionDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Get all positions for an account with symbol details
     */
    public List<Position> findByAccount(int accountId) {
        String sql = "SELECT p.*, s.symbol, s.name as symbol_name " +
                     "FROM positions p " +
                     "JOIN stock_symbols s ON p.symbol_id = s.symbol_id " +
                     "WHERE p.account_id = ? AND p.quantity > 0 " +
                     "ORDER BY s.symbol";
        List<Position> positions = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, accountId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    positions.add(mapResultSetToPosition(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding positions: " + e.getMessage());
            e.printStackTrace();
        }
        return positions;
    }
    
    /**
     * Get position for a specific symbol and account
     */
    public Position findByAccountAndSymbol(int accountId, int symbolId) {
        String sql = "SELECT p.*, s.symbol, s.name as symbol_name " +
                     "FROM positions p " +
                     "JOIN stock_symbols s ON p.symbol_id = s.symbol_id " +
                     "WHERE p.account_id = ? AND p.symbol_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, accountId);
            stmt.setInt(2, symbolId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPosition(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding position: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Create or update position
     */
    public boolean upsert(Position position) {
        String sql = "INSERT INTO positions (account_id, symbol_id, quantity, avg_cost, current_value, unrealized_pnl) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "quantity = VALUES(quantity), " +
                     "avg_cost = VALUES(avg_cost), " +
                     "current_value = VALUES(current_value), " +
                     "unrealized_pnl = VALUES(unrealized_pnl)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, position.getAccountId());
            stmt.setInt(2, position.getSymbolId());
            stmt.setInt(3, position.getQuantity());
            stmt.setBigDecimal(4, position.getAvgCost());
            stmt.setBigDecimal(5, position.getCurrentValue());
            stmt.setBigDecimal(6, position.getUnrealizedPnl());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error upserting position: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Update position values (current price, unrealized P&L)
     */
    public boolean updateValues(int positionId, java.math.BigDecimal currentValue, java.math.BigDecimal unrealizedPnl) {
        String sql = "UPDATE positions SET current_value = ?, unrealized_pnl = ? WHERE position_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, currentValue);
            stmt.setBigDecimal(2, unrealizedPnl);
            stmt.setInt(3, positionId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating position values: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Delete position if quantity is zero
     */
    public boolean deleteIfEmpty(int accountId, int symbolId) {
        String sql = "DELETE FROM positions WHERE account_id = ? AND symbol_id = ? AND quantity = 0";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, accountId);
            stmt.setInt(2, symbolId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting empty position: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Map ResultSet to Position
     */
    private Position mapResultSetToPosition(ResultSet rs) throws SQLException {
        Position position = new Position();
        position.setPositionId(rs.getInt("position_id"));
        position.setAccountId(rs.getInt("account_id"));
        position.setSymbolId(rs.getInt("symbol_id"));
        position.setQuantity(rs.getInt("quantity"));
        position.setAvgCost(rs.getBigDecimal("avg_cost"));
        position.setCurrentValue(rs.getBigDecimal("current_value"));
        position.setUnrealizedPnl(rs.getBigDecimal("unrealized_pnl"));
        
        // Additional fields from join
        position.setSymbol(rs.getString("symbol"));
        position.setSymbolName(rs.getString("symbol_name"));
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            position.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return position;
    }
}
