package com.savora.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model class representing a spending limit for a category or overall spending
 */
public class SpendingLimit {
    private int limitId;
    private Integer categoryId; // null for overall spending limit
    private BigDecimal limitAmount;
    private String period;
    private boolean isActive;
    private LocalDateTime createdAt;
    
    // Constructors
    public SpendingLimit() {}
    
    public SpendingLimit(Integer categoryId, BigDecimal limitAmount, String period) {
        this.categoryId = categoryId;
        this.limitAmount = limitAmount;
        this.period = period;
        this.isActive = true;
    }
    
    public SpendingLimit(int limitId, Integer categoryId, BigDecimal limitAmount, 
                        String period, boolean isActive, LocalDateTime createdAt) {
        this.limitId = limitId;
        this.categoryId = categoryId;
        this.limitAmount = limitAmount;
        this.period = period;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public int getLimitId() {
        return limitId;
    }
    
    public void setLimitId(int limitId) {
        this.limitId = limitId;
    }
    
    public Integer getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
    
    public BigDecimal getLimitAmount() {
        return limitAmount;
    }
    
    public void setLimitAmount(BigDecimal limitAmount) {
        this.limitAmount = limitAmount;
    }
    
    public String getPeriod() {
        return period;
    }
    
    public void setPeriod(String period) {
        this.period = period;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Check if this is an overall spending limit (not category-specific)
     */
    public boolean isOverallLimit() {
        return categoryId == null;
    }
    
    @Override
    public String toString() {
        return "SpendingLimit{" +
                "limitId=" + limitId +
                ", categoryId=" + categoryId +
                ", limitAmount=" + limitAmount +
                ", period='" + period + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}
