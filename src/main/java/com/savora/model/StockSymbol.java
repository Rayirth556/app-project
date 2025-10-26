package com.savora.model;

import java.time.LocalDateTime;

/**
 * Model class for stock symbols
 */
public class StockSymbol {
    private int symbolId;
    private String symbol;
    private String name;
    private LocalDateTime createdAt;
    
    public StockSymbol() {}
    
    public StockSymbol(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }
    
    public int getSymbolId() {
        return symbolId;
    }
    
    public void setSymbolId(int symbolId) {
        this.symbolId = symbolId;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return symbol + " - " + name;
    }
}
