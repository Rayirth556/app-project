package com.savora.dao;

import com.savora.model.Order;
import com.savora.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Order operations
 */
public class OrderDAO {
    private final DatabaseConnection dbConnection;
    
    public OrderDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Create new order
     */
    public boolean create(Order order) {
        String sql = "INSERT INTO orders (account_id, symbol_id, order_type, side, quantity, limit_price, " +
                     "status, filled_quantity, filled_price, order_date, filled_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, order.getAccountId());
            stmt.setInt(2, order.getSymbolId());
            stmt.setString(3, order.getOrderType().name());
            stmt.setString(4, order.getSide().name());
            stmt.setInt(5, order.getQuantity());
            
            if (order.getLimitPrice() != null) {
                stmt.setBigDecimal(6, order.getLimitPrice());
            } else {
                stmt.setNull(6, Types.DECIMAL);
            }
            
            stmt.setString(7, order.getStatus().name());
            stmt.setInt(8, order.getFilledQuantity());
            
            if (order.getFilledPrice() != null) {
                stmt.setBigDecimal(9, order.getFilledPrice());
            } else {
                stmt.setNull(9, Types.DECIMAL);
            }
            
            stmt.setDate(10, Date.valueOf(order.getOrderDate()));
            
            if (order.getFilledDate() != null) {
                stmt.setDate(11, Date.valueOf(order.getFilledDate()));
            } else {
                stmt.setNull(11, Types.DATE);
            }
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        order.setOrderId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating order: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Update order status and filled details
     */
    public boolean updateOrderFilled(long orderId, Order.Status status, int filledQuantity, 
                                     java.math.BigDecimal filledPrice, LocalDate filledDate) {
        String sql = "UPDATE orders SET status = ?, filled_quantity = ?, filled_price = ?, filled_date = ? " +
                     "WHERE order_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.name());
            stmt.setInt(2, filledQuantity);
            stmt.setBigDecimal(3, filledPrice);
            stmt.setDate(4, Date.valueOf(filledDate));
            stmt.setLong(5, orderId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating order: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get all orders for an account
     */
    public List<Order> findByAccount(int accountId) {
        String sql = "SELECT o.*, s.symbol " +
                     "FROM orders o " +
                     "JOIN stock_symbols s ON o.symbol_id = s.symbol_id " +
                     "WHERE o.account_id = ? " +
                     "ORDER BY o.created_at DESC";
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, accountId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding orders: " + e.getMessage());
            e.printStackTrace();
        }
        return orders;
    }
    
    /**
     * Get pending orders for a date
     */
    public List<Order> findPendingByDate(int accountId, LocalDate date) {
        String sql = "SELECT o.*, s.symbol " +
                     "FROM orders o " +
                     "JOIN stock_symbols s ON o.symbol_id = s.symbol_id " +
                     "WHERE o.account_id = ? AND o.status = 'PENDING' AND o.order_date <= ? " +
                     "ORDER BY o.order_date, o.created_at";
        List<Order> orders = new ArrayList<>();
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, accountId);
            stmt.setDate(2, Date.valueOf(date));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding pending orders: " + e.getMessage());
            e.printStackTrace();
        }
        return orders;
    }
    
    /**
     * Map ResultSet to Order
     */
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getLong("order_id"));
        order.setAccountId(rs.getInt("account_id"));
        order.setSymbolId(rs.getInt("symbol_id"));
        order.setOrderType(Order.OrderType.valueOf(rs.getString("order_type")));
        order.setSide(Order.Side.valueOf(rs.getString("side")));
        order.setQuantity(rs.getInt("quantity"));
        
        java.math.BigDecimal limitPrice = rs.getBigDecimal("limit_price");
        if (limitPrice != null) {
            order.setLimitPrice(limitPrice);
        }
        
        order.setStatus(Order.Status.valueOf(rs.getString("status")));
        order.setFilledQuantity(rs.getInt("filled_quantity"));
        
        java.math.BigDecimal filledPrice = rs.getBigDecimal("filled_price");
        if (filledPrice != null) {
            order.setFilledPrice(filledPrice);
        }
        
        order.setOrderDate(rs.getDate("order_date").toLocalDate());
        
        Date filledDate = rs.getDate("filled_date");
        if (filledDate != null) {
            order.setFilledDate(filledDate.toLocalDate());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            order.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        order.setSymbol(rs.getString("symbol"));
        
        return order;
    }
}
