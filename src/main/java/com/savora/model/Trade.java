package com.savora.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model class for executed trades
 */
public class Trade {
    private long tradeId;
    private long orderId;
    private int accountId;
    private int symbolId;
    private Order.Side side;
    private int quantity;
    private BigDecimal price;
    private BigDecimal commission;
    private BigDecimal totalAmount;
    private LocalDate tradeDate;
    private LocalDateTime createdAt;
    
    // For display purposes
    private String symbol;
    
    public Trade() {}
    
    public Trade(long orderId, int accountId, int symbolId, Order.Side side, 
                 int quantity, BigDecimal price, BigDecimal commission, LocalDate tradeDate) {
        this.orderId = orderId;
        this.accountId = accountId;
        this.symbolId = symbolId;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.commission = commission;
        this.tradeDate = tradeDate;
        
        // Calculate total amount
        BigDecimal subtotal = price.multiply(new BigDecimal(quantity));
        if (side == Order.Side.BUY) {
            this.totalAmount = subtotal.add(commission);
        } else {
            this.totalAmount = subtotal.subtract(commission);
        }
    }
    
    // Getters and setters
    public long getTradeId() {
        return tradeId;
    }
    
    public void setTradeId(long tradeId) {
        this.tradeId = tradeId;
    }
    
    public long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }
    
    public int getAccountId() {
        return accountId;
    }
    
    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }
    
    public int getSymbolId() {
        return symbolId;
    }
    
    public void setSymbolId(int symbolId) {
        this.symbolId = symbolId;
    }
    
    public Order.Side getSide() {
        return side;
    }
    
    public void setSide(Order.Side side) {
        this.side = side;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public BigDecimal getCommission() {
        return commission;
    }
    
    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public LocalDate getTradeDate() {
        return tradeDate;
    }
    
    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
