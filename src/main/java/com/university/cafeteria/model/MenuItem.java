package com.university.cafeteria.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MenuItem class representing food and beverage items
 * Demonstrates encapsulation and validation
 */
public class MenuItem {
    private String itemId;
    private String name;
    private String description;
    private BigDecimal price;
    private MenuCategory category;
    private boolean available;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public enum MenuCategory {
        MAIN_COURSE("Main Course", "Full meals and entrees"),
        SNACK("Snack", "Light snacks and appetizers"),
        DRINK("Drink", "Beverages and drinks"),
        DESSERT("Dessert", "Sweet treats and desserts"),
        BREAKFAST("Breakfast", "Morning meals and items");
        
        private final String displayName;
        private final String description;
        
        MenuCategory(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    public MenuItem(String itemId, String name, String description, BigDecimal price, MenuCategory category) {
        validateInputs(itemId, name, price);
        
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.available = true; // Default to available
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Validation method demonstrating input validation
    private void validateInputs(String itemId, String name, BigDecimal price) {
        if (itemId == null || itemId.trim().isEmpty()) {
            throw new IllegalArgumentException("Item ID cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be null or empty");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        // Allow 0.00 price for loyalty redemption items (e.g., free water)
    }
    
    // Getters
    public String getItemId() { return itemId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public MenuCategory getCategory() { return category; }
    public boolean isAvailable() { return available; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    // Setters with validation and update timestamp
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be null or empty");
        }
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setPrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = price;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setCategory(MenuCategory category) {
        this.category = category;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setAvailable(boolean available) {
        this.available = available;
        this.updatedAt = LocalDateTime.now();
    }
    
    // Business logic methods
    public String getFormattedPrice() {
        return String.format("EGP %.2f", price);
    }
    
    public boolean isAffordableFor(BigDecimal budget) {
        return budget.compareTo(price) >= 0;
    }
    
    @Override
    public String toString() {
        return String.format("MenuItem{id='%s', name='%s', price=%s, category='%s', available=%s}", 
            itemId, name, getFormattedPrice(), category.getDisplayName(), available);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MenuItem menuItem = (MenuItem) obj;
        return itemId.equals(menuItem.itemId);
    }
    
    @Override
    public int hashCode() {
        return itemId.hashCode();
    }
}