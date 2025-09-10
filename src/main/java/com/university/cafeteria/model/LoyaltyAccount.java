package com.university.cafeteria.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * LoyaltyAccount class manages loyalty points for students
 * Demonstrates encapsulation and single responsibility principle
 */
public class LoyaltyAccount {
    private int points;
    private List<LoyaltyTransaction> transactions;
    
    public LoyaltyAccount() {
        this.points = 0;
        this.transactions = new ArrayList<>();
    }
    
    public int getPoints() {
        return points;
    }
    
    public List<LoyaltyTransaction> getTransactions() {
        return new ArrayList<>(transactions); // Return defensive copy
    }
    
    /**
     * Add points to the account
     * @param points Points to add (must be positive)
     * @param description Description of the transaction
     */
    public void addPoints(int points, String description) {
        if (points <= 0) {
            throw new IllegalArgumentException("Points to add must be positive");
        }
        
        this.points += points;
        transactions.add(new LoyaltyTransaction(
            LoyaltyTransaction.TransactionType.EARNED, 
            points, 
            description
        ));
    }
    
    /**
     * Convenience method to add points with default description
     */
    public void addPoints(int points) {
        addPoints(points, "Points earned from order");
    }
    
    /**
     * Check if user can redeem specified points
     */
    public boolean canRedeem(int points) {
        return points > 0 && this.points >= points;
    }
    
    /**
     * Redeem points from the account
     * @param points Points to redeem
     * @param description Description of the redemption
     * @return true if redemption successful, false otherwise
     */
    public boolean redeemPoints(int points, String description) {
        if (!canRedeem(points)) {
            return false;
        }
        
        this.points -= points;
        transactions.add(new LoyaltyTransaction(
            LoyaltyTransaction.TransactionType.REDEEMED, 
            points, 
            description
        ));
        return true;
    }
    
    /**
     * Convenience method to redeem points with default description
     */
    public boolean redeemPoints(int points) {
        return redeemPoints(points, "Points redeemed");
    }
    
    /**
     * Add a redemption transaction when loading from database
     * This method doesn't validate balance since we're restoring historical data
     * @param points Points that were redeemed
     * @param description Description of the redemption
     */
    public void addRedemptionTransaction(int points, String description) {
        if (points <= 0) {
            throw new IllegalArgumentException("Points to redeem must be positive");
        }
        
        this.points -= points;
        transactions.add(new LoyaltyTransaction(
            LoyaltyTransaction.TransactionType.REDEEMED, 
            points, 
            description
        ));
    }
    
    /**
     * Inner class representing a loyalty transaction
     * Demonstrates composition and encapsulation
     */
    public static class LoyaltyTransaction {
        public enum TransactionType { EARNED, REDEEMED }
        
        private final TransactionType type;
        private final int points;
        private final String description;
        private final LocalDateTime timestamp;
        
        public LoyaltyTransaction(TransactionType type, int points, String description) {
            this.type = type;
            this.points = points;
            this.description = description;
            this.timestamp = LocalDateTime.now();
        }
        
        // Getters
        public TransactionType getType() { return type; }
        public int getPoints() { return points; }
        public String getDescription() { return description; }
        public LocalDateTime getTimestamp() { return timestamp; }
        
        @Override
        public String toString() {
            return String.format("%s: %d points - %s (%s)", 
                type, points, description, timestamp.toLocalDate());
        }
    }
}