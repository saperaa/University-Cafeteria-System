package com.university.cafeteria.model;

import java.time.LocalDateTime;

/**
 * Abstract base class for all users in the system
 * Demonstrates inheritance and encapsulation principles
 */
public abstract class User {
    protected String userId;
    protected String name;
    protected String password;
    protected LocalDateTime createdAt;
    
    public User(String userId, String name, String password) {
        this.userId = userId;
        this.name = name;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    // Password verification (encapsulated)
    public boolean verifyPassword(String password) {
        return this.password.equals(password);
    }
    
    // Abstract method to be implemented by subclasses
    public abstract String getUserType();
    
    @Override
    public String toString() {
        return String.format("%s{userId='%s', name='%s', type='%s'}", 
            getClass().getSimpleName(), userId, name, getUserType());
    }
}