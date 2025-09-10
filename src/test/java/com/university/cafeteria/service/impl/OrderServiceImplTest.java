package com.university.cafeteria.service.impl;

import com.university.cafeteria.model.*;
import com.university.cafeteria.model.Order.OrderStatus;
import com.university.cafeteria.repository.OrderRepository;
import com.university.cafeteria.service.LoyaltyService;
import com.university.cafeteria.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for OrderServiceImpl
 * Demonstrates unit testing best practices with Mockito and JUnit 5
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private LoyaltyService loyaltyService;
    
    @Mock
    private NotificationService notificationService;
    
    @InjectMocks
    private OrderServiceImpl orderService;
    
    private Order testOrder;
    private MenuItem testMenuItem;
    private final String TEST_STUDENT_ID = "2023001";
    private final String TEST_ORDER_ID = "ORD-12345678";
    
    @BeforeEach
    void setUp() {
        // Create test menu item
        testMenuItem = new MenuItem(
            "ITEM001",
            "Test Burger",
            "Delicious test burger",
            new BigDecimal("25.00"),
            MenuItem.MenuCategory.MAIN_COURSE
        );
        
        // Create test order
        testOrder = new Order(TEST_STUDENT_ID);
        // Simulate order ID generation by setting it directly
        // In a real scenario, this would be handled by the Order constructor
    }
    
    @Test
    void createOrder_WithValidStudentId_ShouldReturnNewOrder() {
        // Given
        Order expectedOrder = new Order(TEST_STUDENT_ID);
        when(orderRepository.save(any(Order.class))).thenReturn(expectedOrder);
        
        // When
        Order result = orderService.createOrder(TEST_STUDENT_ID);
        
        // Then
        assertNotNull(result);
        assertEquals(TEST_STUDENT_ID, result.getStudentId());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertTrue(result.isEmpty());
        
        verify(orderRepository, times(1)).save(any(Order.class));
    }
    
    @Test
    void createOrder_WithNullStudentId_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> orderService.createOrder(null)
        );
        
        assertEquals("Student ID cannot be null or empty", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void createOrder_WithEmptyStudentId_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> orderService.createOrder("   ")
        );
        
        assertEquals("Student ID cannot be null or empty", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }
    
    @Test
    void addItemToOrder_WithValidParameters_ShouldAddItemAndReturnUpdatedOrder() {
        // Given
        when(orderRepository.findById(TEST_ORDER_ID)).thenReturn(Optional.of(testOrder));
        when(orderRepository.update(any(Order.class))).thenReturn(testOrder);
        
        // When
        Order result = orderService.addItemToOrder(TEST_ORDER_ID, testMenuItem, 2);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getItems().size());
        
        Order.OrderItem addedItem = result.getItems().get(0);
        assertEquals(testMenuItem, addedItem.getMenuItem());
        assertEquals(2, addedItem.getQuantity());
        
        verify(orderRepository, times(1)).findById(TEST_ORDER_ID);
        verify(orderRepository, times(1)).update(testOrder);
    }
    
    @Test
    void addItemToOrder_WithNonExistentOrder_ShouldThrowException() {
        // Given
        when(orderRepository.findById(TEST_ORDER_ID)).thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> orderService.addItemToOrder(TEST_ORDER_ID, testMenuItem, 1)
        );
        
        assertEquals("Order not found: " + TEST_ORDER_ID, exception.getMessage());
        verify(orderRepository, times(1)).findById(TEST_ORDER_ID);
        verify(orderRepository, never()).update(any(Order.class));
    }
    
    @Test
    void addItemToOrder_WithUnavailableMenuItem_ShouldThrowException() {
        // Given
        testMenuItem.setAvailable(false);
        when(orderRepository.findById(TEST_ORDER_ID)).thenReturn(Optional.of(testOrder));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> orderService.addItemToOrder(TEST_ORDER_ID, testMenuItem, 1)
        );
        
        assertEquals("Menu item is not available: " + testMenuItem.getName(), exception.getMessage());
        verify(orderRepository, times(1)).findById(TEST_ORDER_ID);
        verify(orderRepository, never()).update(any(Order.class));
    }
    
    @Test
    void addItemToOrder_WithZeroQuantity_ShouldThrowException() {
        // Given
        when(orderRepository.findById(TEST_ORDER_ID)).thenReturn(Optional.of(testOrder));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> orderService.addItemToOrder(TEST_ORDER_ID, testMenuItem, 0)
        );
        
        assertEquals("Quantity must be positive", exception.getMessage());
        verify(orderRepository, times(1)).findById(TEST_ORDER_ID);
        verify(orderRepository, never()).update(any(Order.class));
    }
    
    @Test
    void confirmOrder_WithValidPendingOrder_ShouldConfirmAndAwardPoints() {
        // Given
        testOrder.addItem(testMenuItem, 2);
        testOrder.updateStatus(OrderStatus.PENDING);
        
        when(orderRepository.findById(TEST_ORDER_ID)).thenReturn(Optional.of(testOrder));
        when(orderRepository.update(any(Order.class))).thenReturn(testOrder);
        when(loyaltyService.awardPointsFromOrder(eq(TEST_STUDENT_ID), any(BigDecimal.class), eq(TEST_ORDER_ID)))
            .thenReturn(5);
        
        // When
        Order result = orderService.confirmOrder(TEST_ORDER_ID);
        
        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        
        verify(orderRepository, times(1)).findById(TEST_ORDER_ID);
        verify(orderRepository, times(1)).update(testOrder);
        verify(loyaltyService, times(1)).awardPointsFromOrder(
            eq(TEST_STUDENT_ID), 
            any(BigDecimal.class), 
            eq(TEST_ORDER_ID)
        );
        verify(notificationService, times(1)).notifyOrderConfirmed(testOrder);
    }
    
    @Test
    void confirmOrder_WithEmptyOrder_ShouldThrowException() {
        // Given
        when(orderRepository.findById(TEST_ORDER_ID)).thenReturn(Optional.of(testOrder));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> orderService.confirmOrder(TEST_ORDER_ID)
        );
        
        assertEquals("Cannot confirm empty order", exception.getMessage());
        verify(orderRepository, times(1)).findById(TEST_ORDER_ID);
        verify(orderRepository, never()).update(any(Order.class));
        verify(loyaltyService, never()).awardPointsFromOrder(anyString(), any(BigDecimal.class), anyString());
        verify(notificationService, never()).notifyOrderConfirmed(any(Order.class));
    }
    
    @Test
    void confirmOrder_WithAlreadyConfirmedOrder_ShouldThrowException() {
        // Given
        testOrder.addItem(testMenuItem, 1);
        testOrder.updateStatus(OrderStatus.CONFIRMED);
        
        when(orderRepository.findById(TEST_ORDER_ID)).thenReturn(Optional.of(testOrder));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> orderService.confirmOrder(TEST_ORDER_ID)
        );
        
        assertEquals("Only pending orders can be confirmed", exception.getMessage());
        verify(orderRepository, times(1)).findById(TEST_ORDER_ID);
        verify(orderRepository, never()).update(any(Order.class));
    }
    
    @Test
    void updateOrderStatus_WithValidTransition_ShouldUpdateStatus() {
        // Given
        testOrder.updateStatus(OrderStatus.CONFIRMED);
        
        when(orderRepository.findById(TEST_ORDER_ID)).thenReturn(Optional.of(testOrder));
        when(orderRepository.update(any(Order.class))).thenReturn(testOrder);
        
        // When
        Order result = orderService.updateOrderStatus(TEST_ORDER_ID, OrderStatus.PREPARING);
        
        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.PREPARING, result.getStatus());
        
        verify(orderRepository, times(1)).findById(TEST_ORDER_ID);
        verify(orderRepository, times(1)).update(testOrder);
        verify(notificationService, times(1)).notifyOrderStatusUpdate(
            eq(testOrder), 
            eq("Your order is now being prepared")
        );
    }
    
    @Test
    void updateOrderStatus_WithInvalidTransition_ShouldThrowException() {
        // Given
        testOrder.updateStatus(OrderStatus.PENDING);
        
        when(orderRepository.findById(TEST_ORDER_ID)).thenReturn(Optional.of(testOrder));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> orderService.updateOrderStatus(TEST_ORDER_ID, OrderStatus.READY)
        );
        
        assertEquals("Invalid status transition from PENDING to READY", exception.getMessage());
        verify(orderRepository, times(1)).findById(TEST_ORDER_ID);
        verify(orderRepository, never()).update(any(Order.class));
    }
    
    @Test
    void applyLoyaltyDiscount_WithSufficientPoints_ShouldApplyDiscount() {
        // Given
        testOrder.addItem(testMenuItem, 2);
        int pointsToRedeem = 100;
        BigDecimal discountAmount = new BigDecimal("20.00");
        
        when(orderRepository.findById(TEST_ORDER_ID)).thenReturn(Optional.of(testOrder));
        when(orderRepository.update(any(Order.class))).thenReturn(testOrder);
        when(loyaltyService.canRedeemPoints(TEST_STUDENT_ID, pointsToRedeem)).thenReturn(true);
        when(loyaltyService.calculateDiscountFromPoints(pointsToRedeem)).thenReturn(discountAmount);
        
        // When
        Order result = orderService.applyLoyaltyDiscount(TEST_ORDER_ID, pointsToRedeem);
        
        // Then
        assertNotNull(result);
        assertEquals(pointsToRedeem, result.getLoyaltyPointsRedeemed());
        assertEquals(discountAmount, result.getDiscountAmount());
        
        verify(orderRepository, times(1)).findById(TEST_ORDER_ID);
        verify(orderRepository, times(1)).update(testOrder);
        verify(loyaltyService, times(1)).canRedeemPoints(TEST_STUDENT_ID, pointsToRedeem);
        verify(loyaltyService, times(1)).calculateDiscountFromPoints(pointsToRedeem);
    }
    
    @Test
    void applyLoyaltyDiscount_WithInsufficientPoints_ShouldThrowException() {
        // Given
        testOrder.addItem(testMenuItem, 2);
        int pointsToRedeem = 100;
        
        when(orderRepository.findById(TEST_ORDER_ID)).thenReturn(Optional.of(testOrder));
        when(loyaltyService.canRedeemPoints(TEST_STUDENT_ID, pointsToRedeem)).thenReturn(false);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> orderService.applyLoyaltyDiscount(TEST_ORDER_ID, pointsToRedeem)
        );
        
        assertEquals("Student does not have enough loyalty points", exception.getMessage());
        verify(orderRepository, times(1)).findById(TEST_ORDER_ID);
        verify(orderRepository, never()).update(any(Order.class));
        verify(loyaltyService, times(1)).canRedeemPoints(TEST_STUDENT_ID, pointsToRedeem);
        verify(loyaltyService, never()).calculateDiscountFromPoints(anyInt());
    }
    
    @Test
    void getEstimatedPreparationTime_WithValidOrder_ShouldCalculateCorrectTime() {
        // Given
        testOrder.addItem(testMenuItem, 3);
        when(orderRepository.findById(TEST_ORDER_ID)).thenReturn(Optional.of(testOrder));
        
        // When
        int estimatedTime = orderService.getEstimatedPreparationTime(TEST_ORDER_ID);
        
        // Then
        // Base time (15 min) + (3 items * 2 min per item) = 21 minutes
        assertEquals(21, estimatedTime);
        
        verify(orderRepository, times(1)).findById(TEST_ORDER_ID);
    }
    
    @Test
    void getEstimatedPreparationTime_WithNonExistentOrder_ShouldReturnZero() {
        // Given
        when(orderRepository.findById(TEST_ORDER_ID)).thenReturn(Optional.empty());
        
        // When
        int estimatedTime = orderService.getEstimatedPreparationTime(TEST_ORDER_ID);
        
        // Then
        assertEquals(0, estimatedTime);
        
        verify(orderRepository, times(1)).findById(TEST_ORDER_ID);
    }
    
    @Test
    void removeItemFromOrder_WithExistingItem_ShouldRemoveItemAndReturnUpdatedOrder() {
        // Given
        testOrder.addItem(testMenuItem, 2);
        
        when(orderRepository.findById(TEST_ORDER_ID)).thenReturn(Optional.of(testOrder));
        when(orderRepository.update(any(Order.class))).thenReturn(testOrder);
        
        // When
        Order result = orderService.removeItemFromOrder(TEST_ORDER_ID, testMenuItem.getItemId());
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(orderRepository, times(1)).findById(TEST_ORDER_ID);
        verify(orderRepository, times(1)).update(testOrder);
    }
    
    @Test
    void updateItemQuantity_WithValidQuantity_ShouldUpdateQuantity() {
        // Given
        testOrder.addItem(testMenuItem, 2);
        
        when(orderRepository.findById(TEST_ORDER_ID)).thenReturn(Optional.of(testOrder));
        when(orderRepository.update(any(Order.class))).thenReturn(testOrder);
        
        // When
        Order result = orderService.updateItemQuantity(TEST_ORDER_ID, testMenuItem.getItemId(), 5);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(5, result.getItems().get(0).getQuantity());
        
        verify(orderRepository, times(1)).findById(TEST_ORDER_ID);
        verify(orderRepository, times(1)).update(testOrder);
    }
    
    @Test
    void updateItemQuantity_WithZeroQuantity_ShouldRemoveItem() {
        // Given
        testOrder.addItem(testMenuItem, 2);
        
        when(orderRepository.findById(TEST_ORDER_ID)).thenReturn(Optional.of(testOrder));
        when(orderRepository.update(any(Order.class))).thenReturn(testOrder);
        
        // When
        Order result = orderService.updateItemQuantity(TEST_ORDER_ID, testMenuItem.getItemId(), 0);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(orderRepository, times(1)).findById(TEST_ORDER_ID);
        verify(orderRepository, times(1)).update(testOrder);
    }
    
    @Test
    void cancelOrder_WithCancellableOrder_ShouldCancelAndNotify() {
        // Given
        testOrder.addItem(testMenuItem, 1);
        testOrder.updateStatus(OrderStatus.PENDING);
        
        when(orderRepository.findById(TEST_ORDER_ID)).thenReturn(Optional.of(testOrder));
        when(orderRepository.update(any(Order.class))).thenReturn(testOrder);
        
        // When
        Order result = orderService.cancelOrder(TEST_ORDER_ID);
        
        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        
        verify(orderRepository, times(1)).findById(TEST_ORDER_ID);
        verify(orderRepository, times(1)).update(testOrder);
        verify(notificationService, times(1)).notifyOrderStatusUpdate(
            eq(testOrder), 
            eq("Your order has been cancelled")
        );
    }
    
    @Test
    void cancelOrder_WithNonCancellableOrder_ShouldThrowException() {
        // Given
        testOrder.updateStatus(OrderStatus.READY);
        
        when(orderRepository.findById(TEST_ORDER_ID)).thenReturn(Optional.of(testOrder));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> orderService.cancelOrder(TEST_ORDER_ID)
        );
        
        assertEquals("Order cannot be cancelled in current status: READY", exception.getMessage());
        verify(orderRepository, times(1)).findById(TEST_ORDER_ID);
        verify(orderRepository, never()).update(any(Order.class));
    }
}