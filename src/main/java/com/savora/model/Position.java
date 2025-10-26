package com.savora.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model class for portfolio positions
 */
public class Position {
    private int positionId;
    private int accountId;
    private int symbolId;
    private int quantity;
    private BigDecimal avgCost;
    private BigDecimal currentValue;
    private BigDecimal unrealizedPnl;
    private LocalDateTime updatedAt;
    
    // For display purposes
    private String symbol;
    private String symbolName;
    
    public Position() {}
    
    public Position(int accountId, int symbolId, int quantity, BigDecimal avgCost) {
        this.accountId = accountId;
        this.symbolId = symbolId;
        this.quantity = quantity;
        this.avgCost = avgCost;
    }
    
    // Getters and setters
    public int getPositionId() {
        return positionId;
    }
    
    public void setPositionId(int positionId) {
        this.positionId = positionId;
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
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getAvgCost() {
        return avgCost;
    }
    
    public void setAvgCost(BigDecimal avgCost) {
        this.avgCost = avgCost;
    }
    
    public BigDecimal getCurrentValue() {
        return currentValue;
    }
    
    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = currentValue;
    }
    
    public BigDecimal getUnrealizedPnl() {
        return unrealizedPnl;
    }
    
    public void setUnrealizedPnl(BigDecimal unrealizedPnl) {
        this.unrealizedPnl = unrealizedPnl;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public String getSymbolName() {
        return symbolName;
    }
    
    public void setSymbolName(String symbolName) {
        this.symbolName = symbolName;
    }
}
