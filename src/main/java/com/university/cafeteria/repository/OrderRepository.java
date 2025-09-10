package com.university.cafeteria.repository;

import com.university.cafeteria.model.Order;
import com.university.cafeteria.model.Order.OrderStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interface for order data access operations
 * Demonstrates Dependency Inversion Principle
 */
public interface OrderRepository {
    
    /**
     * Save an order
     * @param order Order to save
     * @return Saved order
     */
    Order save(Order order);
    
    /**
     * Find order by ID
     * @param orderId Order ID to search for
     * @return Optional containing order if found
     */
    Optional<Order> findById(String orderId);
    
    /**
     * Find all orders for a specific student
     * @param studentId Student ID to search for
     * @return List of orders for the student
     */
    List<Order> findByStudentId(String studentId);
    
    /**
     * Find orders by status
     * @param status Order status to filter by
     * @return List of orders with the specified status
     */
    List<Order> findByStatus(OrderStatus status);
    
    /**
     * Find orders for a specific date
     * @param date Date to search for
     * @return List of orders placed on the date
     */
    List<Order> findByDate(LocalDate date);
    
    /**
     * Find orders between two dates (inclusive)
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of orders between the dates
     */
    List<Order> findByDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find recent orders for a student (last N orders)
     * @param studentId Student ID
     * @param limit Maximum number of orders to return
     * @return List of recent orders
     */
    List<Order> findRecentByStudentId(String studentId, int limit);
    
    /**
     * Find pending orders (for staff processing)
     * @return List of pending orders
     */
    List<Order> findPendingOrders();
    
    /**
     * Find orders in preparation (being prepared)
     * @return List of orders being prepared
     */
    List<Order> findOrdersInPreparation();
    
    /**
     * Find orders ready for pickup
     * @return List of orders ready for pickup
     */
    List<Order> findOrdersReadyForPickup();
    
    /**
     * Get all orders
     * @return List of all orders
     */
    List<Order> findAll();
    
    /**
     * Update order
     * @param order Order to update
     * @return Updated order
     */
    Order update(Order order);
    
    /**
     * Delete order by ID
     * @param orderId Order ID to delete
     * @return true if deleted, false if not found
     */
    boolean deleteById(String orderId);
    
    /**
     * Check if order ID exists
     * @param orderId Order ID to check
     * @return true if exists, false otherwise
     */
    boolean existsById(String orderId);
    
    /**
     * Get count of orders by status
     * @return Array where index represents status ordinal and value is count
     */
    long[] getCountByStatus();
}