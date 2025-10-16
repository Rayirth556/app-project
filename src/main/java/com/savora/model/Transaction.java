package com.savora.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Model class representing a financial transaction
 */
public class Transaction {
    private int transactionId;
    private BigDecimal amount;
    private LocalDate date;
    private int categoryId;
    private String description;
    private TransactionType type;
    
    public enum TransactionType {
        INCOME("income"),
        EXPENSE("expense");
        
        private final String value;
        
        TransactionType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static TransactionType fromString(String value) {
            for (TransactionType type : TransactionType.values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid transaction type: " + value);
        }
        
        @Override
        public String toString() {
            return value.substring(0, 1).toUpperCase() + value.substring(1);
        }
    }
    
    // Constructors
    public Transaction() {}
    
    public Transaction(BigDecimal amount, LocalDate date, int categoryId, String description, TransactionType type) {
        this.amount = amount;
        this.date = date;
        this.categoryId = categoryId;
        this.description = description;
        this.type = type;
    }
    
    public Transaction(int transactionId, BigDecimal amount, LocalDate date, int categoryId, String description, TransactionType type) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.date = date;
        this.categoryId = categoryId;
        this.description = description;
        this.type = type;
    }
    
    // Getters and Setters
    public int getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public int getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public TransactionType getType() {
        return type;
    }
    
    public void setType(TransactionType type) {
        this.type = type;
    }
    
    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", amount=" + amount +
                ", date=" + date +
                ", categoryId=" + categoryId +
                ", description='" + description + '\'' +
                ", type=" + type +
                '}';
    }
}
