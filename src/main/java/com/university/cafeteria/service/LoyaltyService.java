package com.university.cafeteria.service;

import com.university.cafeteria.model.Student;
import com.university.cafeteria.model.LoyaltyAccount.LoyaltyTransaction;
import java.math.BigDecimal;
import java.util.List;

/**
 * Interface for loyalty program services
 * Demonstrates Single Responsibility Principle - focused on loyalty operations only
 */
public interface LoyaltyService {
    
    /**
     * Calculate loyalty points earned from an order amount
     * @param orderAmount Order total amount
     * @return Points earned
     */
    int calculatePointsEarned(BigDecimal orderAmount);
    
    /**
     * Award loyalty points to a student
     * @param studentId Student ID
     * @param points Points to award
     * @param description Description of the transaction
     * @return true if points awarded successfully
     */
    boolean awardPoints(String studentId, int points, String description);
    
    /**
     * Award loyalty points based on order amount
     * @param studentId Student ID
     * @param orderAmount Order amount
     * @param orderId Order ID for reference
     * @return Points awarded
     */
    int awardPointsFromOrder(String studentId, BigDecimal orderAmount, String orderId);
    
    /**
     * Check if student can redeem specified points
     * @param studentId Student ID
     * @param points Points to redeem
     * @return true if redemption is possible
     */
    boolean canRedeemPoints(String studentId, int points);
    
    /**
     * Redeem loyalty points for discount
     * @param studentId Student ID
     * @param points Points to redeem
     * @param description Description of the redemption
     * @return Discount amount in currency
     */
    BigDecimal redeemPointsForDiscount(String studentId, int points, String description);
    
    /**
     * Get current loyalty points balance for a student
     * @param studentId Student ID
     * @return Current points balance
     */
    int getPointsBalance(String studentId);
    
    /**
     * Get loyalty transaction history for a student
     * @param studentId Student ID
     * @return List of loyalty transactions
     */
    List<LoyaltyTransaction> getTransactionHistory(String studentId);
    
    /**
     * Get recent loyalty transactions for a student
     * @param studentId Student ID
     * @param limit Maximum number of transactions to return
     * @return List of recent transactions
     */
    List<LoyaltyTransaction> getRecentTransactions(String studentId, int limit);
    
    /**
     * Calculate discount amount for given points
     * @param points Points to convert to discount
     * @return Discount amount in currency
     */
    BigDecimal calculateDiscountFromPoints(int points);
    
    /**
     * Calculate points required for a specific discount amount
     * @param discountAmount Desired discount amount
     * @return Points required
     */
    int calculatePointsForDiscount(BigDecimal discountAmount);
    
    /**
     * Get available redemption options
     * @param currentPoints Student's current points
     * @return List of available redemption options
     */
    List<RedemptionOption> getAvailableRedemptions(int currentPoints);
    
    /**
     * Inner class representing a redemption option
     */
    class RedemptionOption {
        private final int pointsRequired;
        private final BigDecimal discountAmount;
        private final String description;
        
        public RedemptionOption(int pointsRequired, BigDecimal discountAmount, String description) {
            this.pointsRequired = pointsRequired;
            this.discountAmount = discountAmount;
            this.description = description;
        }
        
        public int getPointsRequired() { return pointsRequired; }
        public BigDecimal getDiscountAmount() { return discountAmount; }
        public String getDescription() { return description; }
        
        @Override
        public String toString() {
            return String.format("%s: %d points = EGP %.2f discount", 
                description, pointsRequired, discountAmount);
        }
    }
}