package com.university.cafeteria.service.impl;

import com.university.cafeteria.model.Student;
import com.university.cafeteria.model.LoyaltyAccount.LoyaltyTransaction;
import com.university.cafeteria.repository.UserRepository;
import com.university.cafeteria.service.NotificationService;
import com.university.cafeteria.service.LoyaltyService.RedemptionOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for LoyaltyServiceImpl
 * Demonstrates testing of service layer with repository and notification dependencies
 */
@ExtendWith(MockitoExtension.class)
class LoyaltyServiceImplTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private NotificationService notificationService;
    
    @InjectMocks
    private LoyaltyServiceImpl loyaltyService;
    
    private Student testStudent;
    private final String TEST_STUDENT_ID = "2023001";
    private final String TEST_ORDER_ID = "ORD-12345678";
    
    @BeforeEach
    void setUp() {
        testStudent = new Student("student1", "John Smith", "password", TEST_STUDENT_ID);
        // Give student some initial points for testing
        testStudent.addLoyaltyPoints(150);
    }
    
    @Test
    void calculatePointsEarned_WithValidAmount_ShouldReturnCorrectPoints() {
        // Given
        BigDecimal orderAmount = new BigDecimal("50.00");
        
        // When
        int points = loyaltyService.calculatePointsEarned(orderAmount);
        
        // Then
        assertEquals(5, points); // 50 / 10 = 5 points
    }
    
    @Test
    void calculatePointsEarned_WithAmountLessThanTen_ShouldReturnZero() {
        // Given
        BigDecimal orderAmount = new BigDecimal("9.99");
        
        // When
        int points = loyaltyService.calculatePointsEarned(orderAmount);
        
        // Then
        assertEquals(0, points); // 9.99 / 10 = 0 points (floor division)
    }
    
    @Test
    void calculatePointsEarned_WithZeroAmount_ShouldReturnZero() {
        // Given
        BigDecimal orderAmount = BigDecimal.ZERO;
        
        // When
        int points = loyaltyService.calculatePointsEarned(orderAmount);
        
        // Then
        assertEquals(0, points);
    }
    
    @Test
    void calculatePointsEarned_WithNullAmount_ShouldReturnZero() {
        // When
        int points = loyaltyService.calculatePointsEarned(null);
        
        // Then
        assertEquals(0, points);
    }
    
    @Test
    void awardPoints_WithValidStudentAndPoints_ShouldAwardPointsAndReturnTrue() {
        // Given
        when(userRepository.findStudentByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.of(testStudent));
        when(userRepository.update(testStudent)).thenReturn(testStudent);
        
        int initialPoints = testStudent.getLoyaltyPoints();
        int pointsToAward = 50;
        String description = "Test bonus points";
        
        // When
        boolean result = loyaltyService.awardPoints(TEST_STUDENT_ID, pointsToAward, description);
        
        // Then
        assertTrue(result);
        assertEquals(initialPoints + pointsToAward, testStudent.getLoyaltyPoints());
        
        verify(userRepository, times(1)).findStudentByStudentId(TEST_STUDENT_ID);
        verify(userRepository, times(1)).update(testStudent);
    }
    
    @Test
    void awardPoints_WithNonExistentStudent_ShouldReturnFalse() {
        // Given
        when(userRepository.findStudentByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.empty());
        
        // When
        boolean result = loyaltyService.awardPoints(TEST_STUDENT_ID, 50, "Test points");
        
        // Then
        assertFalse(result);
        
        verify(userRepository, times(1)).findStudentByStudentId(TEST_STUDENT_ID);
        verify(userRepository, never()).update(any(Student.class));
    }
    
    @Test
    void awardPoints_WithZeroPoints_ShouldReturnFalse() {
        // When
        boolean result = loyaltyService.awardPoints(TEST_STUDENT_ID, 0, "Test points");
        
        // Then
        assertFalse(result);
        
        verify(userRepository, never()).findStudentByStudentId(anyString());
        verify(userRepository, never()).update(any(Student.class));
    }
    
    @Test
    void awardPointsFromOrder_WithValidOrder_ShouldAwardPointsAndNotify() {
        // Given
        BigDecimal orderAmount = new BigDecimal("100.00");
        
        when(userRepository.findStudentByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.of(testStudent));
        when(userRepository.update(testStudent)).thenReturn(testStudent);
        
        int initialPoints = testStudent.getLoyaltyPoints();
        
        // When
        int pointsAwarded = loyaltyService.awardPointsFromOrder(TEST_STUDENT_ID, orderAmount, TEST_ORDER_ID);
        
        // Then
        assertEquals(10, pointsAwarded); // 100 / 10 = 10 points
        assertEquals(initialPoints + 10, testStudent.getLoyaltyPoints());
        
        verify(userRepository, times(1)).findStudentByStudentId(TEST_STUDENT_ID);
        verify(userRepository, times(1)).update(testStudent);
        verify(notificationService, times(1)).notifyPointsEarned(TEST_STUDENT_ID, 10, TEST_ORDER_ID);
    }
    
    @Test
    void awardPointsFromOrder_WithSmallOrder_ShouldNotAwardPointsOrNotify() {
        // Given
        BigDecimal orderAmount = new BigDecimal("5.00");
        
        // When
        int pointsAwarded = loyaltyService.awardPointsFromOrder(TEST_STUDENT_ID, orderAmount, TEST_ORDER_ID);
        
        // Then
        assertEquals(0, pointsAwarded);
        
        verify(userRepository, never()).findStudentByStudentId(anyString());
        verify(userRepository, never()).update(any(Student.class));
        verify(notificationService, never()).notifyPointsEarned(anyString(), anyInt(), anyString());
    }
    
    @Test
    void canRedeemPoints_WithSufficientPoints_ShouldReturnTrue() {
        // Given
        when(userRepository.findStudentByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.of(testStudent));
        
        // When
        boolean canRedeem = loyaltyService.canRedeemPoints(TEST_STUDENT_ID, 100);
        
        // Then
        assertTrue(canRedeem);
        
        verify(userRepository, times(1)).findStudentByStudentId(TEST_STUDENT_ID);
    }
    
    @Test
    void canRedeemPoints_WithInsufficientPoints_ShouldReturnFalse() {
        // Given
        when(userRepository.findStudentByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.of(testStudent));
        
        // When
        boolean canRedeem = loyaltyService.canRedeemPoints(TEST_STUDENT_ID, 200); // Student has 150
        
        // Then
        assertFalse(canRedeem);
        
        verify(userRepository, times(1)).findStudentByStudentId(TEST_STUDENT_ID);
    }
    
    @Test
    void canRedeemPoints_WithMinimumPoints_ShouldReturnTrue() {
        // Given
        when(userRepository.findStudentByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.of(testStudent));
        
        // When
        boolean canRedeem = loyaltyService.canRedeemPoints(TEST_STUDENT_ID, 10); // Minimum is 10
        
        // Then
        assertTrue(canRedeem);
        
        verify(userRepository, times(1)).findStudentByStudentId(TEST_STUDENT_ID);
    }
    
    @Test
    void canRedeemPoints_WithBelowMinimumPoints_ShouldReturnFalse() {
        // When
        boolean canRedeem = loyaltyService.canRedeemPoints(TEST_STUDENT_ID, 5); // Below minimum
        
        // Then
        assertFalse(canRedeem);
        
        verify(userRepository, never()).findStudentByStudentId(anyString());
    }
    
    @Test
    void redeemPointsForDiscount_WithValidRedemption_ShouldRedeemAndNotify() {
        // Given
        int pointsToRedeem = 100;
        BigDecimal expectedDiscount = new BigDecimal("20.00");
        
        when(userRepository.findStudentByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.of(testStudent));
        when(userRepository.update(testStudent)).thenReturn(testStudent);
        
        int initialPoints = testStudent.getLoyaltyPoints();
        
        // When
        BigDecimal discount = loyaltyService.redeemPointsForDiscount(TEST_STUDENT_ID, pointsToRedeem, "Test redemption");
        
        // Then
        assertEquals(expectedDiscount, discount);
        assertEquals(initialPoints - pointsToRedeem, testStudent.getLoyaltyPoints());
        
        verify(userRepository, times(2)).findStudentByStudentId(TEST_STUDENT_ID); // canRedeem + redeemPoints
        verify(userRepository, times(1)).update(testStudent);
        verify(notificationService, times(1)).notifyPointsRedeemed(TEST_STUDENT_ID, pointsToRedeem, expectedDiscount);
    }
    
    @Test
    void redeemPointsForDiscount_WithInsufficientPoints_ShouldThrowException() {
        // Given
        when(userRepository.findStudentByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.of(testStudent));
        
        int pointsToRedeem = 200; // Student has 150
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> loyaltyService.redeemPointsForDiscount(TEST_STUDENT_ID, pointsToRedeem, "Test redemption")
        );
        
        assertEquals("Cannot redeem 200 points for student " + TEST_STUDENT_ID, exception.getMessage());
        
        verify(userRepository, times(1)).findStudentByStudentId(TEST_STUDENT_ID);
        verify(userRepository, never()).update(any(Student.class));
        verify(notificationService, never()).notifyPointsRedeemed(anyString(), anyInt(), any(BigDecimal.class));
    }
    
    @Test
    void getPointsBalance_WithExistingStudent_ShouldReturnBalance() {
        // Given
        when(userRepository.findStudentByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.of(testStudent));
        
        // When
        int balance = loyaltyService.getPointsBalance(TEST_STUDENT_ID);
        
        // Then
        assertEquals(150, balance);
        
        verify(userRepository, times(1)).findStudentByStudentId(TEST_STUDENT_ID);
    }
    
    @Test
    void getPointsBalance_WithNonExistentStudent_ShouldReturnZero() {
        // Given
        when(userRepository.findStudentByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.empty());
        
        // When
        int balance = loyaltyService.getPointsBalance(TEST_STUDENT_ID);
        
        // Then
        assertEquals(0, balance);
        
        verify(userRepository, times(1)).findStudentByStudentId(TEST_STUDENT_ID);
    }
    
    @Test
    void getTransactionHistory_WithExistingStudent_ShouldReturnHistory() {
        // Given
        when(userRepository.findStudentByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.of(testStudent));
        
        // When
        List<LoyaltyTransaction> history = loyaltyService.getTransactionHistory(TEST_STUDENT_ID);
        
        // Then
        assertNotNull(history);
        assertEquals(1, history.size()); // Student was given 150 points in setUp
        
        verify(userRepository, times(1)).findStudentByStudentId(TEST_STUDENT_ID);
    }
    
    @Test
    void getRecentTransactions_WithValidLimit_ShouldReturnLimitedHistory() {
        // Given
        // Add multiple transactions
        testStudent.addLoyaltyPoints(50);
        testStudent.addLoyaltyPoints(30);
        testStudent.addLoyaltyPoints(20);
        
        when(userRepository.findStudentByStudentId(TEST_STUDENT_ID)).thenReturn(Optional.of(testStudent));
        
        // When
        List<LoyaltyTransaction> recent = loyaltyService.getRecentTransactions(TEST_STUDENT_ID, 2);
        
        // Then
        assertNotNull(recent);
        assertEquals(2, recent.size());
        
        verify(userRepository, times(1)).findStudentByStudentId(TEST_STUDENT_ID);
    }
    
    @Test
    void calculateDiscountFromPoints_WithValidPoints_ShouldReturnCorrectDiscount() {
        // When
        BigDecimal discount = loyaltyService.calculateDiscountFromPoints(100);
        
        // Then
        assertEquals(new BigDecimal("20.00"), discount); // 100 * 0.20 = 20.00
    }
    
    @Test
    void calculateDiscountFromPoints_WithZeroPoints_ShouldReturnZero() {
        // When
        BigDecimal discount = loyaltyService.calculateDiscountFromPoints(0);
        
        // Then
        assertEquals(BigDecimal.ZERO, discount);
    }
    
    @Test
    void calculatePointsForDiscount_WithValidDiscount_ShouldReturnCorrectPoints() {
        // Given
        BigDecimal discountAmount = new BigDecimal("10.00");
        
        // When
        int points = loyaltyService.calculatePointsForDiscount(discountAmount);
        
        // Then
        assertEquals(50, points); // 10.00 / 0.20 = 50 points
    }
    
    @Test
    void getAvailableRedemptions_WithSufficientPoints_ShouldReturnMultipleOptions() {
        // When
        List<RedemptionOption> options = loyaltyService.getAvailableRedemptions(200);
        
        // Then
        assertNotNull(options);
        assertTrue(options.size() > 0);
        
        // Should include multiple tiers
        boolean hasSmallDiscount = options.stream().anyMatch(opt -> opt.getPointsRequired() == 50);
        boolean hasMediumDiscount = options.stream().anyMatch(opt -> opt.getPointsRequired() == 100);
        boolean hasSpecialItem = options.stream().anyMatch(opt -> opt.getPointsRequired() == 150);
        
        assertTrue(hasSmallDiscount);
        assertTrue(hasMediumDiscount);
        assertTrue(hasSpecialItem);
    }
    
    @Test
    void getAvailableRedemptions_WithLowPoints_ShouldReturnLimitedOptions() {
        // When
        List<RedemptionOption> options = loyaltyService.getAvailableRedemptions(60);
        
        // Then
        assertNotNull(options);
        
        // Should only include options with 60 or fewer points
        boolean allAffordable = options.stream().allMatch(opt -> opt.getPointsRequired() <= 60);
        assertTrue(allAffordable);
        
        // Should not include high-tier options
        boolean hasHighTierOption = options.stream().anyMatch(opt -> opt.getPointsRequired() > 60);
        assertFalse(hasHighTierOption);
    }
    
    @Test
    void getAvailableRedemptions_WithInsufficientPoints_ShouldReturnEmptyList() {
        // When
        List<RedemptionOption> options = loyaltyService.getAvailableRedemptions(30);
        
        // Then
        assertNotNull(options);
        assertTrue(options.isEmpty());
    }
}