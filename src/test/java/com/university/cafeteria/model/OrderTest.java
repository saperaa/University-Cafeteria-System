package com.university.cafeteria.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Order entity
 * Demonstrates testing of domain models and business logic
 */
class OrderTest {
    
    private Order order;
    private MenuItem testMenuItem1;
    private MenuItem testMenuItem2;
    private final String TEST_STUDENT_ID = "2023001";
    
    @BeforeEach
    void setUp() {
        order = new Order(TEST_STUDENT_ID);
        
        testMenuItem1 = new MenuItem(
            "ITEM001",
            "Burger",
            "Delicious burger",
            new BigDecimal("25.00"),
            MenuItem.MenuCategory.MAIN_COURSE
        );
        
        testMenuItem2 = new MenuItem(
            "ITEM002",
            "Fries",
            "Crispy fries",
            new BigDecimal("10.00"),
            MenuItem.MenuCategory.SNACK
        );
    }
    
    @Test
    void constructor_WithValidStudentId_ShouldCreateEmptyPendingOrder() {
        // Then
        assertNotNull(order.getOrderId());
        assertTrue(order.getOrderId().startsWith("ORD-"));
        assertEquals(TEST_STUDENT_ID, order.getStudentId());
        assertEquals(Order.OrderStatus.PENDING, order.getStatus());
        assertTrue(order.isEmpty());
        assertEquals(BigDecimal.ZERO, order.getTotalAmount());
        assertEquals(0, order.getTotalItemCount());
        assertNotNull(order.getOrderTime());
    }
    
    @Test
    void addItem_WithValidMenuItem_ShouldAddItemAndUpdateTotals() {
        // When
        order.addItem(testMenuItem1, 2);
        
        // Then
        assertFalse(order.isEmpty());
        assertEquals(1, order.getItems().size());
        assertEquals(2, order.getTotalItemCount());
        
        Order.OrderItem addedItem = order.getItems().get(0);
        assertEquals(testMenuItem1, addedItem.getMenuItem());
        assertEquals(2, addedItem.getQuantity());
        assertEquals(new BigDecimal("50.00"), addedItem.getSubtotal());
        
        assertEquals(new BigDecimal("50.00"), order.getTotalAmount());
        assertEquals(5, order.getLoyaltyPointsEarned()); // 50/10 = 5 points
    }
    
    @Test
    void addItem_WithSameMenuItemTwice_ShouldCombineQuantities() {
        // When
        order.addItem(testMenuItem1, 2);
        order.addItem(testMenuItem1, 3);
        
        // Then
        assertEquals(1, order.getItems().size());
        assertEquals(5, order.getTotalItemCount());
        
        Order.OrderItem item = order.getItems().get(0);
        assertEquals(5, item.getQuantity());
        assertEquals(new BigDecimal("125.00"), item.getSubtotal());
        assertEquals(new BigDecimal("125.00"), order.getTotalAmount());
    }
    
    @Test
    void addItem_WithMultipleDifferentItems_ShouldAddSeparately() {
        // When
        order.addItem(testMenuItem1, 2);
        order.addItem(testMenuItem2, 3);
        
        // Then
        assertEquals(2, order.getItems().size());
        assertEquals(5, order.getTotalItemCount());
        assertEquals(new BigDecimal("80.00"), order.getTotalAmount()); // 50 + 30
        assertEquals(8, order.getLoyaltyPointsEarned()); // 80/10 = 8 points
    }
    
    @Test
    void addItem_WithNullMenuItem_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> order.addItem(null, 1)
        );
        
        assertEquals("Menu item cannot be null", exception.getMessage());
        assertTrue(order.isEmpty());
    }
    
    @Test
    void addItem_WithZeroQuantity_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> order.addItem(testMenuItem1, 0)
        );
        
        assertEquals("Quantity must be positive", exception.getMessage());
        assertTrue(order.isEmpty());
    }
    
    @Test
    void addItem_WithUnavailableMenuItem_ShouldThrowException() {
        // Given
        testMenuItem1.setAvailable(false);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> order.addItem(testMenuItem1, 1)
        );
        
        assertEquals("Menu item is not available", exception.getMessage());
        assertTrue(order.isEmpty());
    }
    
    @Test
    void removeItem_WithExistingItem_ShouldRemoveItemAndUpdateTotals() {
        // Given
        order.addItem(testMenuItem1, 2);
        order.addItem(testMenuItem2, 1);
        
        // When
        boolean removed = order.removeItem(testMenuItem1.getItemId());
        
        // Then
        assertTrue(removed);
        assertEquals(1, order.getItems().size());
        assertEquals(1, order.getTotalItemCount());
        assertEquals(new BigDecimal("10.00"), order.getTotalAmount());
        assertEquals(testMenuItem2, order.getItems().get(0).getMenuItem());
    }
    
    @Test
    void removeItem_WithNonExistentItem_ShouldReturnFalse() {
        // Given
        order.addItem(testMenuItem1, 1);
        
        // When
        boolean removed = order.removeItem("NON_EXISTENT_ID");
        
        // Then
        assertFalse(removed);
        assertEquals(1, order.getItems().size());
    }
    
    @Test
    void updateItemQuantity_WithValidQuantity_ShouldUpdateQuantityAndTotals() {
        // Given
        order.addItem(testMenuItem1, 2);
        
        // When
        boolean updated = order.updateItemQuantity(testMenuItem1.getItemId(), 5);
        
        // Then
        assertTrue(updated);
        assertEquals(1, order.getItems().size());
        assertEquals(5, order.getTotalItemCount());
        assertEquals(5, order.getItems().get(0).getQuantity());
        assertEquals(new BigDecimal("125.00"), order.getTotalAmount());
    }
    
    @Test
    void updateItemQuantity_WithZeroQuantity_ShouldRemoveItem() {
        // Given
        order.addItem(testMenuItem1, 2);
        order.addItem(testMenuItem2, 1);
        
        // When
        boolean updated = order.updateItemQuantity(testMenuItem1.getItemId(), 0);
        
        // Then
        assertTrue(updated);
        assertEquals(1, order.getItems().size());
        assertEquals(testMenuItem2, order.getItems().get(0).getMenuItem());
    }
    
    @Test
    void updateItemQuantity_WithNonExistentItem_ShouldReturnFalse() {
        // Given
        order.addItem(testMenuItem1, 1);
        
        // When
        boolean updated = order.updateItemQuantity("NON_EXISTENT_ID", 5);
        
        // Then
        assertFalse(updated);
        assertEquals(1, order.getItems().size());
        assertEquals(1, order.getItems().get(0).getQuantity());
    }
    
    @Test
    void applyLoyaltyDiscount_WithValidDiscount_ShouldApplyDiscountAndUpdateTotal() {
        // Given
        order.addItem(testMenuItem1, 4); // 100 EGP
        
        // When
        boolean applied = order.applyLoyaltyDiscount(50, new BigDecimal("10.00"));
        
        // Then
        assertTrue(applied);
        assertEquals(50, order.getLoyaltyPointsRedeemed());
        assertEquals(new BigDecimal("10.00"), order.getDiscountAmount());
        assertEquals(new BigDecimal("90.00"), order.getTotalAmount()); // 100 - 10
        assertEquals(9, order.getLoyaltyPointsEarned()); // 90/10 = 9 points
    }
    
    @Test
    void applyLoyaltyDiscount_WithZeroPoints_ShouldReturnFalse() {
        // Given
        order.addItem(testMenuItem1, 2);
        
        // When
        boolean applied = order.applyLoyaltyDiscount(0, new BigDecimal("5.00"));
        
        // Then
        assertFalse(applied);
        assertEquals(0, order.getLoyaltyPointsRedeemed());
        assertEquals(BigDecimal.ZERO, order.getDiscountAmount());
    }
    
    @Test
    void updateStatus_ShouldUpdateStatusAndTimestamp() {
        // Given
        assertNotNull(order.getStatusUpdatedTime());
        
        // When
        try {
            Thread.sleep(1); // Ensure timestamp difference
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        order.updateStatus(Order.OrderStatus.CONFIRMED);
        
        // Then
        assertEquals(Order.OrderStatus.CONFIRMED, order.getStatus());
        assertNotNull(order.getStatusUpdatedTime());
    }
    
    @Test
    void canBeCancelled_WithPendingOrder_ShouldReturnTrue() {
        // Given
        order.updateStatus(Order.OrderStatus.PENDING);
        
        // When & Then
        assertTrue(order.canBeCancelled());
    }
    
    @Test
    void canBeCancelled_WithConfirmedOrder_ShouldReturnTrue() {
        // Given
        order.updateStatus(Order.OrderStatus.CONFIRMED);
        
        // When & Then
        assertTrue(order.canBeCancelled());
    }
    
    @Test
    void canBeCancelled_WithPreparingOrder_ShouldReturnFalse() {
        // Given
        order.updateStatus(Order.OrderStatus.PREPARING);
        
        // When & Then
        assertFalse(order.canBeCancelled());
    }
    
    @Test
    void canBeCancelled_WithCompletedOrder_ShouldReturnFalse() {
        // Given
        order.updateStatus(Order.OrderStatus.COMPLETED);
        
        // When & Then
        assertFalse(order.canBeCancelled());
    }
    
    @Test
    void isCompleted_WithCompletedOrder_ShouldReturnTrue() {
        // Given
        order.updateStatus(Order.OrderStatus.COMPLETED);
        
        // When & Then
        assertTrue(order.isCompleted());
    }
    
    @Test
    void isCompleted_WithCancelledOrder_ShouldReturnTrue() {
        // Given
        order.updateStatus(Order.OrderStatus.CANCELLED);
        
        // When & Then
        assertTrue(order.isCompleted());
    }
    
    @Test
    void isCompleted_WithPendingOrder_ShouldReturnFalse() {
        // Given
        order.updateStatus(Order.OrderStatus.PENDING);
        
        // When & Then
        assertFalse(order.isCompleted());
    }
    
    @Test
    void getUniqueMenuItems_ShouldReturnListOfUniqueItems() {
        // Given
        order.addItem(testMenuItem1, 2);
        order.addItem(testMenuItem2, 1);
        order.addItem(testMenuItem1, 1); // Should not create duplicate
        
        // When
        var uniqueItems = order.getUniqueMenuItems();
        
        // Then
        assertEquals(2, uniqueItems.size());
        assertTrue(uniqueItems.contains(testMenuItem1));
        assertTrue(uniqueItems.contains(testMenuItem2));
    }
    
    @Test
    void getFormattedTotal_ShouldReturnFormattedCurrency() {
        // Given
        order.addItem(testMenuItem1, 2);
        
        // When
        String formattedTotal = order.getFormattedTotal();
        
        // Then
        assertEquals("EGP 50.00", formattedTotal);
    }
    
    @Test
    void toString_ShouldReturnReadableRepresentation() {
        // Given
        order.addItem(testMenuItem1, 1);
        
        // When
        String orderString = order.toString();
        
        // Then
        assertTrue(orderString.contains(order.getOrderId()));
        assertTrue(orderString.contains(TEST_STUDENT_ID));
        assertTrue(orderString.contains("1")); // items count
        assertTrue(orderString.contains("EGP 25.00"));
        assertTrue(orderString.contains("PENDING"));
    }
}