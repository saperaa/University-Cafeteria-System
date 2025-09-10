package com.university.cafeteria.service;

import com.university.cafeteria.model.Order;
import com.university.cafeteria.model.Order.OrderStatus;
import com.university.cafeteria.model.MenuItem;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interface for order management services
 * Demonstrates Single Responsibility Principle - focused on order operations only
 */
public interface OrderService {
    
    /**
     * Create a new order for a student
     * @param studentId Student ID
     * @return New order instance
     */
    Order createOrder(String studentId);
    
    /**
     * Add item to an existing order
     * @param orderId Order ID
     * @param menuItem Menu item to add
     * @param quantity Quantity to add
     * @return Updated order
     * @throws IllegalArgumentException if order not found or validation fails
     */
    Order addItemToOrder(String orderId, MenuItem menuItem, int quantity);
    
    /**
     * Remove item from an order
     * @param orderId Order ID
     * @param itemId Menu item ID to remove
     * @return Updated order
     * @throws IllegalArgumentException if order not found
     */
    Order removeItemFromOrder(String orderId, String itemId);
    
    /**
     * Update item quantity in an order
     * @param orderId Order ID
     * @param itemId Menu item ID
     * @param newQuantity New quantity
     * @return Updated order
     * @throws IllegalArgumentException if order not found or validation fails
     */
    Order updateItemQuantity(String orderId, String itemId, int newQuantity);
    
    /**
     * Apply loyalty discount to an order
     * @param orderId Order ID
     * @param pointsToRedeem Points to redeem
     * @return Updated order with discount applied
     * @throws IllegalArgumentException if order not found or insufficient points
     */
    Order applyLoyaltyDiscount(String orderId, int pointsToRedeem);
    
    /**
     * Confirm and place an order
     * @param orderId Order ID
     * @return Confirmed order
     * @throws IllegalArgumentException if order not found or cannot be confirmed
     */
    Order confirmOrder(String orderId);
    
    /**
     * Cancel an order
     * @param orderId Order ID
     * @return Cancelled order
     * @throws IllegalArgumentException if order not found or cannot be cancelled
     */
    Order cancelOrder(String orderId);
    
    /**
     * Update order status (staff only)
     * @param orderId Order ID
     * @param newStatus New status
     * @return Updated order
     * @throws IllegalArgumentException if order not found or invalid status transition
     */
    Order updateOrderStatus(String orderId, OrderStatus newStatus);
    
    /**
     * Get order by ID
     * @param orderId Order ID
     * @return Optional containing order if found
     */
    Optional<Order> getOrderById(String orderId);
    
    /**
     * Get all orders for a student
     * @param studentId Student ID
     * @return List of orders for the student
     */
    List<Order> getOrdersByStudent(String studentId);
    
    /**
     * Get recent orders for a student
     * @param studentId Student ID
     * @param limit Maximum number of orders to return
     * @return List of recent orders
     */
    List<Order> getRecentOrdersByStudent(String studentId, int limit);
    
    /**
     * Get orders by status (for staff)
     * @param status Order status
     * @return List of orders with the specified status
     */
    List<Order> getOrdersByStatus(OrderStatus status);
    
    /**
     * Get pending orders for processing
     * @return List of pending orders
     */
    List<Order> getPendingOrders();
    
    /**
     * Get orders in preparation
     * @return List of orders being prepared
     */
    List<Order> getOrdersInPreparation();
    
    /**
     * Get orders ready for pickup
     * @return List of orders ready for pickup
     */
    List<Order> getOrdersReadyForPickup();
    
    /**
     * Get orders for a specific date
     * @param date Date to search for
     * @return List of orders placed on the date
     */
    List<Order> getOrdersByDate(LocalDate date);
    
    /**
     * Get orders between two dates
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of orders between the dates
     */
    List<Order> getOrdersByDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * Calculate delivery/preparation time estimate
     * @param orderId Order ID
     * @return Estimated time in minutes
     */
    int getEstimatedPreparationTime(String orderId);
    
    /**
     * Get order statistics
     * @return Array of order counts by status
     */
    long[] getOrderStatistics();
}