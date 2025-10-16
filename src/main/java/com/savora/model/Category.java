package com.savora.model;

import java.time.LocalDateTime;

/**
 * Model class representing a transaction category
 */
public class Category {
    private int categoryId;
    private String name;
    private boolean isDefault;
    private LocalDateTime createdAt;
    
    // Constructors
    public Category() {}
    
    public Category(String name) {
        this.name = name;
        this.isDefault = false;
    }
    
    public Category(String name, boolean isDefault) {
        this.name = name;
        this.isDefault = isDefault;
    }
    
    public Category(int categoryId, String name, boolean isDefault, LocalDateTime createdAt) {
        this.categoryId = categoryId;
        this.name = name;
        this.isDefault = isDefault;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public int getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public boolean isDefault() {
        return isDefault;
    }
    
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "Category{" +
                "categoryId=" + categoryId +
                ", name='" + name + '\'' +
                ", isDefault=" + isDefault +
                ", createdAt=" + createdAt +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Category category = (Category) obj;
        return categoryId == category.categoryId;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(categoryId);
    }
}
