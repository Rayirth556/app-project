package com.savora.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Model class for historical market data
 */
public class MarketData {
    private long dataId;
    private int symbolId;
    private LocalDate tradeDate;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal closePrice;
    private BigDecimal adjustedClose;
    private long volume;
    
    public MarketData() {}
    
    public MarketData(int symbolId, LocalDate tradeDate, BigDecimal openPrice, 
                      BigDecimal highPrice, BigDecimal lowPrice, BigDecimal closePrice,
                      BigDecimal adjustedClose, long volume) {
        this.symbolId = symbolId;
        this.tradeDate = tradeDate;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.adjustedClose = adjustedClose;
        this.volume = volume;
    }
    
    // Getters and setters
    public long getDataId() {
        return dataId;
    }
    
    public void setDataId(long dataId) {
        this.dataId = dataId;
    }
    
    public int getSymbolId() {
        return symbolId;
    }
    
    public void setSymbolId(int symbolId) {
        this.symbolId = symbolId;
    }
    
    public LocalDate getTradeDate() {
        return tradeDate;
    }
    
    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }
    
    public BigDecimal getOpenPrice() {
        return openPrice;
    }
    
    public void setOpenPrice(BigDecimal openPrice) {
        this.openPrice = openPrice;
    }
    
    public BigDecimal getHighPrice() {
        return highPrice;
    }
    
    public void setHighPrice(BigDecimal highPrice) {
        this.highPrice = highPrice;
    }
    
    public BigDecimal getLowPrice() {
        return lowPrice;
    }
    
    public void setLowPrice(BigDecimal lowPrice) {
        this.lowPrice = lowPrice;
    }
    
    public BigDecimal getClosePrice() {
        return closePrice;
    }
    
    public void setClosePrice(BigDecimal closePrice) {
        this.closePrice = closePrice;
    }
    
    public BigDecimal getAdjustedClose() {
        return adjustedClose;
    }
    
    public void setAdjustedClose(BigDecimal adjustedClose) {
        this.adjustedClose = adjustedClose;
    }
    
    public long getVolume() {
        return volume;
    }
    
    public void setVolume(long volume) {
        this.volume = volume;
    }
}
