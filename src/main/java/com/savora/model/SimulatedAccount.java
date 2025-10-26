package com.savora.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model class for simulated trading account
 */
public class SimulatedAccount {
    private int accountId;
    private String accountName;
    private BigDecimal initialCash;
    private BigDecimal currentCash;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public SimulatedAccount() {}
    
    public SimulatedAccount(String accountName, BigDecimal initialCash) {
        this.accountName = accountName;
        this.initialCash = initialCash;
        this.currentCash = initialCash;
    }
    
    // Getters and setters
    public int getAccountId() {
        return accountId;
    }
    
    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }
    
    public String getAccountName() {
        return accountName;
    }
    
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
    
    public BigDecimal getInitialCash() {
        return initialCash;
    }
    
    public void setInitialCash(BigDecimal initialCash) {
        this.initialCash = initialCash;
    }
    
    public BigDecimal getCurrentCash() {
        return currentCash;
    }
    
    public void setCurrentCash(BigDecimal currentCash) {
        this.currentCash = currentCash;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
