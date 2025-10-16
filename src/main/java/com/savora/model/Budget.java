package com.savora.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model class representing a budget for a specific category
 */
public class Budget {
    private int budgetId;
    private int categoryId;
    private BigDecimal amount;
    private String period;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isActive;
    private LocalDateTime createdAt;
    
    // Constructors
    public Budget() {}
    
    public Budget(int categoryId, BigDecimal amount, String period, LocalDate startDate) {
        this.categoryId = categoryId;
        this.amount = amount;
        this.period = period;
        this.startDate = startDate;
        this.isActive = true;
    }
    
    public Budget(int budgetId, int categoryId, BigDecimal amount, String period, 
                  LocalDate startDate, LocalDate endDate, boolean isActive, LocalDateTime createdAt) {
        this.budgetId = budgetId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.period = period;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public int getBudgetId() {
        return budgetId;
    }
    
    public void setBudgetId(int budgetId) {
        this.budgetId = budgetId;
    }
    
    public int getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getPeriod() {
        return period;
    }
    
    public void setPeriod(String period) {
        this.period = period;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
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
    
    @Override
    public String toString() {
        return "Budget{" +
                "budgetId=" + budgetId +
                ", categoryId=" + categoryId +
                ", amount=" + amount +
                ", period='" + period + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}
