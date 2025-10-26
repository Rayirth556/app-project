package com.savora.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model class for stock orders
 */
public class Order {
    private long orderId;
    private int accountId;
    private int symbolId;
    private OrderType orderType;
    private Side side;
    private int quantity;
    private BigDecimal limitPrice;
    private Status status;
    private int filledQuantity;
    private BigDecimal filledPrice;
    private LocalDate orderDate;
    private LocalDate filledDate;
    private LocalDateTime createdAt;
    
    // For display purposes
    private String symbol;
    
    public Order() {}
    
    public Order(int accountId, int symbolId, OrderType orderType, Side side, 
                 int quantity, BigDecimal limitPrice, LocalDate orderDate) {
        this.accountId = accountId;
        this.symbolId = symbolId;
        this.orderType = orderType;
        this.side = side;
        this.quantity = quantity;
        this.limitPrice = limitPrice;
        this.orderDate = orderDate;
        this.status = Status.PENDING;
        this.filledQuantity = 0;
    }
    
    public enum OrderType {
        MARKET, LIMIT
    }
    
    public enum Side {
        BUY, SELL
    }
    
    public enum Status {
        PENDING, FILLED, CANCELLED, REJECTED
    }
    
    // Getters and setters
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
    
    public OrderType getOrderType() {
        return orderType;
    }
    
    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }
    
    public Side getSide() {
        return side;
    }
    
    public void setSide(Side side) {
        this.side = side;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getLimitPrice() {
        return limitPrice;
    }
    
    public void setLimitPrice(BigDecimal limitPrice) {
        this.limitPrice = limitPrice;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public int getFilledQuantity() {
        return filledQuantity;
    }
    
    public void setFilledQuantity(int filledQuantity) {
        this.filledQuantity = filledQuantity;
    }
    
    public BigDecimal getFilledPrice() {
        return filledPrice;
    }
    
    public void setFilledPrice(BigDecimal filledPrice) {
        this.filledPrice = filledPrice;
    }
    
    public LocalDate getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }
    
    public LocalDate getFilledDate() {
        return filledDate;
    }
    
    public void setFilledDate(LocalDate filledDate) {
        this.filledDate = filledDate;
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
