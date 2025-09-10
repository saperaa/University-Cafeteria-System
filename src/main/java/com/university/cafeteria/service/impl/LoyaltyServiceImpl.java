package com.university.cafeteria.service.impl;

import com.university.cafeteria.model.Student;
import com.university.cafeteria.model.LoyaltyAccount.LoyaltyTransaction;
import com.university.cafeteria.repository.UserRepository;
import com.university.cafeteria.service.LoyaltyService;
import com.university.cafeteria.service.NotificationService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of LoyaltyService
 * Demonstrates Single Responsibility Principle - handles only loyalty program logic
 * Uses Dependency Injection and extensive stream operations
 */
public class LoyaltyServiceImpl implements LoyaltyService {
    
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    
    // Loyalty program configuration
    private static final BigDecimal POINTS_PER_CURRENCY_UNIT = new BigDecimal("10"); // 1 point per 10 EGP
    private static final BigDecimal CURRENCY_PER_POINT = new BigDecimal("0.20"); // 1 point = 0.20 EGP discount
    private static final int MIN_REDEMPTION_POINTS = 10; // Minimum points that can be redeemed
    
    // Special redemption values as per requirements
    private static final int POINTS_FOR_10_EGP = 50; // 50 points = 10 EGP
    private static final BigDecimal DISCOUNT_FOR_50_POINTS = new BigDecimal("10.00"); // 50 points = 10 EGP
    private static final int POINTS_FOR_FREE_WATER = 100; // 100 points = free water
    
    public LoyaltyServiceImpl(UserRepository userRepository, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }
    
    @Override
    public int calculatePointsEarned(BigDecimal orderAmount) {
        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        
        // Calculate points: 1 point per 10 EGP spent
        return orderAmount.divide(POINTS_PER_CURRENCY_UNIT, 0, RoundingMode.DOWN).intValue();
    }
    
    @Override
    public boolean awardPoints(String studentId, int points, String description) {
        if (studentId == null || points <= 0) {
            return false;
        }
        
        Optional<Student> studentOpt = userRepository.findStudentByStudentId(studentId);
        if (!studentOpt.isPresent()) {
            return false;
        }
        
        Student student = studentOpt.get();
        student.getLoyaltyAccount().addPoints(points, description);
        userRepository.update(student);
        
        // Persist the earned points transaction to database
        persistEarnedPointsToDatabase(studentId, points, description);
        
        return true;
    }
    
    @Override
    public int awardPointsFromOrder(String studentId, BigDecimal orderAmount, String orderId) {
        int pointsEarned = calculatePointsEarned(orderAmount);
        
        if (pointsEarned > 0) {
            String description = "Points earned from order " + orderId + " (EGP " + orderAmount + ")";
            if (awardPoints(studentId, pointsEarned, description)) {
                // Send notification
                notificationService.notifyPointsEarned(studentId, pointsEarned, orderId);
                return pointsEarned;
            }
        }
        
        return 0;
    }
    
    @Override
    public boolean canRedeemPoints(String studentId, int points) {
        if (studentId == null || points < MIN_REDEMPTION_POINTS) {
            return false;
        }
        
        Optional<Student> studentOpt = userRepository.findStudentByStudentId(studentId);
        if (!studentOpt.isPresent()) {
            return false;
        }
        
        return studentOpt.get().canRedeemPoints(points);
    }
    
    @Override
    public BigDecimal redeemPointsForDiscount(String studentId, int points, String description) {
        if (!canRedeemPoints(studentId, points)) {
            throw new IllegalArgumentException("Cannot redeem " + points + " points for student " + studentId);
        }
        
        Optional<Student> studentOpt = userRepository.findStudentByStudentId(studentId);
        if (!studentOpt.isPresent()) {
            throw new IllegalArgumentException("Student not found: " + studentId);
        }
        
        Student student = studentOpt.get();
        BigDecimal discountAmount = calculateDiscountFromPoints(points);
        
        if (student.getLoyaltyAccount().redeemPoints(points, description)) {
            userRepository.update(student);
            
            // Persist the loyalty transaction to database
            persistLoyaltyTransactionToDatabase(studentId, "REDEEMED", points, description);
            
            // Send notification
            notificationService.notifyPointsRedeemed(studentId, points, discountAmount);
            
            return discountAmount;
        }
        
        throw new RuntimeException("Failed to redeem points for student " + studentId);
    }
    
    @Override
    public int getPointsBalance(String studentId) {
        if (studentId == null) {
            return 0;
        }
        
        Optional<Student> studentOpt = userRepository.findStudentByStudentId(studentId);
        return studentOpt.map(Student::getLoyaltyPoints).orElse(0);
    }
    
    @Override
    public List<LoyaltyTransaction> getTransactionHistory(String studentId) {
        if (studentId == null) {
            return new ArrayList<>();
        }
        
        Optional<Student> studentOpt = userRepository.findStudentByStudentId(studentId);
        if (!studentOpt.isPresent()) {
            return new ArrayList<>();
        }
        
        return studentOpt.get().getLoyaltyAccount().getTransactions();
    }
    
    @Override
    public List<LoyaltyTransaction> getRecentTransactions(String studentId, int limit) {
        if (limit <= 0) {
            return new ArrayList<>();
        }
        
        List<LoyaltyTransaction> allTransactions = getTransactionHistory(studentId);
        
        return allTransactions.stream()
                .sorted((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp())) // Most recent first
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public BigDecimal calculateDiscountFromPoints(int points) {
        if (points <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Handle special redemption cases as per requirements
        if (points == POINTS_FOR_10_EGP) {
            return DISCOUNT_FOR_50_POINTS; // 50 points = 10 EGP
        } else if (points == POINTS_FOR_FREE_WATER) {
            return BigDecimal.ZERO; // 100 points = free water (0 EGP discount)
        }
        
        // Default calculation for other point values
        return CURRENCY_PER_POINT.multiply(BigDecimal.valueOf(points))
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public int calculatePointsForDiscount(BigDecimal discountAmount) {
        if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        
        return discountAmount.divide(CURRENCY_PER_POINT, 0, RoundingMode.UP).intValue();
    }
    
    @Override
    public List<RedemptionOption> getAvailableRedemptions(int currentPoints) {
        List<RedemptionOption> options = new ArrayList<>();
        
        // Special redemption cases as per requirements
        if (currentPoints >= POINTS_FOR_10_EGP) {
            options.add(new RedemptionOption(50, DISCOUNT_FOR_50_POINTS, "50 points = EGP 10 discount"));
        }
        
        if (currentPoints >= POINTS_FOR_FREE_WATER) {
            options.add(new RedemptionOption(100, BigDecimal.ZERO, "100 points = Free Water"));
        }
        
        // Additional redemption tiers for other point values
        if (currentPoints >= 200) {
            options.add(new RedemptionOption(200, calculateDiscountFromPoints(200), "200 points = EGP " + calculateDiscountFromPoints(200) + " discount"));
        }
        
        if (currentPoints >= 500) {
            options.add(new RedemptionOption(500, calculateDiscountFromPoints(500), "500 points = EGP " + calculateDiscountFromPoints(500) + " discount"));
        }
        
        return options;
    }
    
    /**
     * Persist loyalty transaction to database
     */
    private void persistLoyaltyTransactionToDatabase(String studentId, String type, int points, String description) {
        try {
            // Database connection details
            String dbUrl = "jdbc:mysql://127.0.0.1:3306/cafedb";
            String dbUser = "root";
            String dbPassword = "12345";
            
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                String sql = "INSERT INTO loyalty_transactions (student_id, type, points, description, timestamp) VALUES (?,?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, studentId);
                    ps.setString(2, type);
                    ps.setInt(3, points);
                    ps.setString(4, description);
                    ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                    ps.executeUpdate();
                }
                System.out.println("✅ Successfully persisted loyalty transaction to database: " + type + " " + points + " points for " + studentId);
            }
        } catch (SQLException ex) {
            System.err.println("❌ Error persisting loyalty transaction to database: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Persist earned points transaction to database
     */
    private void persistEarnedPointsToDatabase(String studentId, int points, String description) {
        persistLoyaltyTransactionToDatabase(studentId, "EARNED", points, description);
    }
}