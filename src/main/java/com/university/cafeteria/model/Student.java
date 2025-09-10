package com.university.cafeteria.model;

/**
 * Student class representing university students
 * Extends User and includes loyalty program functionality
 * Demonstrates inheritance and composition with LoyaltyAccount
 */
public class Student extends User {
    private String studentId;
    private LoyaltyAccount loyaltyAccount;
    
    public Student(String userId, String name, String password, String studentId) {
        super(userId, name, password);
        this.studentId = studentId;
        this.loyaltyAccount = new LoyaltyAccount();
    }
    
    @Override
    public String getUserType() {
        return "STUDENT";
    }
    
    // Student-specific getters
    public String getStudentId() { return studentId; }
    public LoyaltyAccount getLoyaltyAccount() { return loyaltyAccount; }
    
    // Loyalty point convenience methods
    public int getLoyaltyPoints() {
        return loyaltyAccount.getPoints();
    }
    
    public void addLoyaltyPoints(int points) {
        loyaltyAccount.addPoints(points);
    }
    
    public boolean canRedeemPoints(int points) {
        return loyaltyAccount.canRedeem(points);
    }
    
    public boolean redeemPoints(int points) {
        return loyaltyAccount.redeemPoints(points);
    }
    
    @Override
    public String toString() {
        return String.format("Student{userId='%s', name='%s', studentId='%s', loyaltyPoints=%d}", 
            userId, name, studentId, getLoyaltyPoints());
    }
}