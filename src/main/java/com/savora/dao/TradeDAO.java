package com.savora.dao;

import com.savora.model.Order;
import com.savora.model.Trade;
import com.savora.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Trade operations
 */
public class TradeDAO {
    private final DatabaseConnection dbConnection;
    
    public TradeDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Create new trade
     */
    public boolean create(Trade trade) {
        String sql = "INSERT INTO trades (order_id, account_id, symbol_id, side, quantity, price, " +
                     "commission, total_amount, trade_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, trade.getOrderId());
            stmt.setInt(2, trade.getAccountId());
            stmt.setInt(3, trade.getSymbolId());
            stmt.setString(4, trade.getSide().name());
            stmt.setInt(5, trade.getQuantity());
            stmt.setBigDecimal(6, trade.getPrice());
            stmt.setBigDecimal(7, trade.getCommission());
            stmt.setBigDecimal(8, trade.getTotalAmount());
            stmt.setDate(9, Date.valueOf(trade.getTradeDate()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        trade.setTradeId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating trade: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get all trades for an account
     */
    public List<Trade> findByAccount(int accountId) {
        String sql = "SELECT t.*, s.symbol " +
                     "FROM trades t " +
                     "JOIN stock_symbols s ON t.symbol_id = s.symbol_id " +
                     "WHERE t.account_id = ? " +
                     "ORDER BY t.trade_date DESC, t.created_at DESC";
        List<Trade> trades = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, accountId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    trades.add(mapResultSetToTrade(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding trades: " + e.getMessage());
            e.printStackTrace();
        }
        return trades;
    }
    
    /**
     * Get trades for an account within date range
     */
    public List<Trade> findByAccountAndDateRange(int accountId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT t.*, s.symbol " +
                     "FROM trades t " +
                     "JOIN stock_symbols s ON t.symbol_id = s.symbol_id " +
                     "WHERE t.account_id = ? AND t.trade_date BETWEEN ? AND ? " +
                     "ORDER BY t.trade_date, t.created_at";
        List<Trade> trades = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, accountId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    trades.add(mapResultSetToTrade(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding trades by date range: " + e.getMessage());
            e.printStackTrace();
        }
        return trades;
    }
    
    /**
     * Map ResultSet to Trade
     */
    private Trade mapResultSetToTrade(ResultSet rs) throws SQLException {
        Trade trade = new Trade();
        trade.setTradeId(rs.getLong("trade_id"));
        trade.setOrderId(rs.getLong("order_id"));
        trade.setAccountId(rs.getInt("account_id"));
        trade.setSymbolId(rs.getInt("symbol_id"));
        trade.setSide(Order.Side.valueOf(rs.getString("side")));
        trade.setQuantity(rs.getInt("quantity"));
        trade.setPrice(rs.getBigDecimal("price"));
        trade.setCommission(rs.getBigDecimal("commission"));
        trade.setTotalAmount(rs.getBigDecimal("total_amount"));
        trade.setTradeDate(rs.getDate("trade_date").toLocalDate());
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            trade.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        trade.setSymbol(rs.getString("symbol"));
        
        return trade;
    }
}
