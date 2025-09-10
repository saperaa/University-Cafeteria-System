package com.university.cafeteria.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Order class representing a customer order
 * Demonstrates composition, streams usage, and business logic encapsulation
 */
public class Order {
    private String orderId;
    private String studentId;
    private List<OrderItem> items;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private int loyaltyPointsEarned;
    private int loyaltyPointsRedeemed;
    private BigDecimal discountAmount;
    private LocalDateTime orderTime;
    private LocalDateTime statusUpdatedTime;
    private String notes;
    
    public enum OrderStatus {
        PENDING("Pending", "Order placed, waiting for processing"),
        CONFIRMED("Confirmed", "Order confirmed and payment processed"),
        PREPARING("Preparing", "Order is being prepared"),
        READY("Ready for Pickup", "Order is ready for pickup"),
        COMPLETED("Completed", "Order has been picked up"),
        CANCELLED("Cancelled", "Order has been cancelled");
        
        private final String displayName;
        private final String description;
        
        OrderStatus(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    public Order(String studentId) {
        this.orderId = generateOrderId();
        this.studentId = studentId;
        this.items = new ArrayList<>();
        this.status = OrderStatus.PENDING;
        this.totalAmount = BigDecimal.ZERO;
        this.loyaltyPointsEarned = 0;
        this.loyaltyPointsRedeemed = 0;
        this.discountAmount = BigDecimal.ZERO;
        this.orderTime = LocalDateTime.now();
        this.statusUpdatedTime = LocalDateTime.now();
        this.notes = "";
    }
    
    // Generate unique order ID
    private String generateOrderId() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    // Getters
    public String getOrderId() { return orderId; }
    public String getStudentId() { return studentId; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public int getLoyaltyPointsEarned() { return loyaltyPointsEarned; }
    public int getLoyaltyPointsRedeemed() { return loyaltyPointsRedeemed; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public LocalDateTime getOrderTime() { return orderTime; }
    public LocalDateTime getStatusUpdatedTime() { return statusUpdatedTime; }
    public String getNotes() { return notes; }
    
    // Add item to order
    public void addItem(MenuItem menuItem, int quantity) {
        if (menuItem == null) {
            throw new IllegalArgumentException("Menu item cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (!menuItem.isAvailable()) {
            throw new IllegalArgumentException("Menu item is not available");
        }
        
        // Check if item already exists in order
        OrderItem existingItem = items.stream()
            .filter(item -> item.getMenuItem().getItemId().equals(menuItem.getItemId()))
            .findFirst()
            .orElse(null);
            
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            items.add(new OrderItem(menuItem, quantity));
        }
        
        calculateTotals();
    }
    
    // Remove item from order
    public boolean removeItem(String itemId) {
        boolean removed = items.removeIf(item -> item.getMenuItem().getItemId().equals(itemId));
        if (removed) {
            calculateTotals();
        }
        return removed;
    }
    
    // Update item quantity
    public boolean updateItemQuantity(String itemId, int newQuantity) {
        if (newQuantity <= 0) {
            return removeItem(itemId);
        }
        
        OrderItem item = items.stream()
            .filter(orderItem -> orderItem.getMenuItem().getItemId().equals(itemId))
            .findFirst()
            .orElse(null);
            
        if (item != null) {
            item.setQuantity(newQuantity);
            calculateTotals();
            return true;
        }
        return false;
    }
    
    // Calculate totals using streams
    public void calculateTotals() {
        // Calculate subtotal from items
        BigDecimal subtotal = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        // Apply discount if it exists
        this.totalAmount = subtotal.subtract(discountAmount);
        
        // Ensure total doesn't go below zero
        if (this.totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            this.totalAmount = BigDecimal.ZERO;
        }
            
        // Calculate loyalty points (1 point per 10 EGP spent)
        this.loyaltyPointsEarned = totalAmount.divide(BigDecimal.TEN).intValue();
        
        // Debug logging
        System.out.println("🔄 calculateTotals() called:");
        System.out.println("   - Items subtotal: " + subtotal);
        System.out.println("   - Discount amount: " + discountAmount);
        System.out.println("   - Final total: " + totalAmount);
        System.out.println("   - Points earned: " + loyaltyPointsEarned);
    }
    
    // Apply discount from loyalty points
    public boolean applyLoyaltyDiscount(int pointsToRedeem, BigDecimal discountValue) {
        if (pointsToRedeem <= 0 || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        this.loyaltyPointsRedeemed = pointsToRedeem;
        this.discountAmount = discountValue;
        calculateTotals();
        return true;
    }
    
    // Status management
    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;
        this.statusUpdatedTime = LocalDateTime.now();
    }
    
    public void setNotes(String notes) {
        this.notes = notes != null ? notes : "";
    }
    
    // Database loading setter methods
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public void setLoyaltyPointsEarned(int loyaltyPointsEarned) {
        this.loyaltyPointsEarned = loyaltyPointsEarned;
    }
    
    public void setLoyaltyPointsRedeemed(int loyaltyPointsRedeemed) {
        this.loyaltyPointsRedeemed = loyaltyPointsRedeemed;
    }
    
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }
    
    public void setStatusUpdatedTime(LocalDateTime statusUpdatedTime) {
        this.statusUpdatedTime = statusUpdatedTime;
    }
    
    // Business logic methods
    public boolean isEmpty() {
        return items.isEmpty();
    }
    
    public int getTotalItemCount() {
        return items.stream()
            .mapToInt(OrderItem::getQuantity)
            .sum();
    }
    
    public List<MenuItem> getUniqueMenuItems() {
        return items.stream()
            .map(OrderItem::getMenuItem)
            .collect(Collectors.toList());
    }
    
    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }
    
    public boolean isCompleted() {
        return status == OrderStatus.COMPLETED || status == OrderStatus.CANCELLED;
    }
    
    public String getFormattedTotal() {
        return String.format("EGP %.2f", totalAmount);
    }
    
    @Override
    public String toString() {
        return String.format("Order{id='%s', studentId='%s', items=%d, total=%s, status='%s'}", 
            orderId, studentId, items.size(), getFormattedTotal(), status.getDisplayName());
    }
    
    /**
     * Inner class representing an item within an order
     * Demonstrates composition and encapsulation
     */
    public static class OrderItem {
        private MenuItem menuItem;
        private int quantity;
        private BigDecimal subtotal;
        
        public OrderItem(MenuItem menuItem, int quantity) {
            this.menuItem = menuItem;
            this.quantity = quantity;
            calculateSubtotal();
        }
        
        public MenuItem getMenuItem() { return menuItem; }
        public int getQuantity() { return quantity; }
        public BigDecimal getSubtotal() { return subtotal; }
        
        public void setQuantity(int quantity) {
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            this.quantity = quantity;
            calculateSubtotal();
        }
        
        private void calculateSubtotal() {
            this.subtotal = menuItem.getPrice().multiply(BigDecimal.valueOf(quantity));
        }
        
        public String getFormattedSubtotal() {
            return String.format("EGP %.2f", subtotal);
        }
        
        @Override
        public String toString() {
            return String.format("%dx %s - %s", 
                quantity, menuItem.getName(), getFormattedSubtotal());
        }
    }
}