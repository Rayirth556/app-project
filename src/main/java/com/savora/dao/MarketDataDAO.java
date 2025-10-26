package com.savora.dao;

import com.savora.model.MarketData;
import com.savora.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for MarketData operations
 */
public class MarketDataDAO {
    private final DatabaseConnection dbConnection;
    
    public MarketDataDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Create market data entry
     */
    public boolean create(MarketData marketData) {
        String sql = "INSERT INTO market_data (symbol_id, trade_date, open_price, high_price, low_price, " +
                     "close_price, adjusted_close, volume) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, marketData.getSymbolId());
            stmt.setDate(2, Date.valueOf(marketData.getTradeDate()));
            stmt.setBigDecimal(3, marketData.getOpenPrice());
            stmt.setBigDecimal(4, marketData.getHighPrice());
            stmt.setBigDecimal(5, marketData.getLowPrice());
            stmt.setBigDecimal(6, marketData.getClosePrice());
            stmt.setBigDecimal(7, marketData.getAdjustedClose());
            stmt.setLong(8, marketData.getVolume());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        marketData.setDataId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating market data: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Batch insert market data for performance
     */
    public int batchInsert(List<MarketData> dataList) {
        String sql = "INSERT IGNORE INTO market_data (symbol_id, trade_date, open_price, high_price, low_price, " +
                     "close_price, adjusted_close, volume) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        int count = 0;
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            
            for (MarketData data : dataList) {
                stmt.setInt(1, data.getSymbolId());
                stmt.setDate(2, Date.valueOf(data.getTradeDate()));
                stmt.setBigDecimal(3, data.getOpenPrice());
                stmt.setBigDecimal(4, data.getHighPrice());
                stmt.setBigDecimal(5, data.getLowPrice());
                stmt.setBigDecimal(6, data.getClosePrice());
                stmt.setBigDecimal(7, data.getAdjustedClose());
                stmt.setLong(8, data.getVolume());
                stmt.addBatch();
                
                if (++count % 500 == 0) {
                    stmt.executeBatch();
                }
            }
            
            stmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            
        } catch (SQLException e) {
            System.err.println("Error batch inserting market data: " + e.getMessage());
            e.printStackTrace();
        }
        return count;
    }
    
    /**
     * Get market data for a symbol on a specific date
     */
    public MarketData findBySymbolAndDate(int symbolId, LocalDate date) {
        String sql = "SELECT * FROM market_data WHERE symbol_id = ? AND trade_date = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, symbolId);
            stmt.setDate(2, Date.valueOf(date));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMarketData(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding market data: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get market data for a symbol within date range
     */
    public List<MarketData> findBySymbolAndDateRange(int symbolId, LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT * FROM market_data WHERE symbol_id = ? AND trade_date BETWEEN ? AND ? " +
                     "ORDER BY trade_date ASC";
        List<MarketData> dataList = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, symbolId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    dataList.add(mapResultSetToMarketData(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding market data range: " + e.getMessage());
            e.printStackTrace();
        }
        return dataList;
    }
    
    /**
     * Get latest market data for a symbol
     */
    public MarketData findLatestBySymbol(int symbolId) {
        String sql = "SELECT * FROM market_data WHERE symbol_id = ? ORDER BY trade_date DESC LIMIT 1";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, symbolId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMarketData(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding latest market data: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get count of data points for a symbol
     */
    public int getDataCountForSymbol(int symbolId) {
        String sql = "SELECT COUNT(*) FROM market_data WHERE symbol_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, symbolId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error counting market data: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Delete all market data for a symbol
     */
    public boolean deleteBySymbolId(int symbolId) {
        String sql = "DELETE FROM market_data WHERE symbol_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, symbolId);
            stmt.executeUpdate();
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error deleting market data for symbol " + symbolId + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Map ResultSet to MarketData
     */
    private MarketData mapResultSetToMarketData(ResultSet rs) throws SQLException {
        MarketData data = new MarketData();
        data.setDataId(rs.getLong("data_id"));
        data.setSymbolId(rs.getInt("symbol_id"));
        data.setTradeDate(rs.getDate("trade_date").toLocalDate());
        data.setOpenPrice(rs.getBigDecimal("open_price"));
        data.setHighPrice(rs.getBigDecimal("high_price"));
        data.setLowPrice(rs.getBigDecimal("low_price"));
        data.setClosePrice(rs.getBigDecimal("close_price"));
        data.setAdjustedClose(rs.getBigDecimal("adjusted_close"));
        data.setVolume(rs.getLong("volume"));
        return data;
    }
}
