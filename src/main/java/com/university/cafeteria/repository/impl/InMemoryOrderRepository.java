package com.university.cafeteria.repository.impl;

import com.university.cafeteria.model.Order;
import com.university.cafeteria.model.Order.OrderStatus;
import com.university.cafeteria.repository.OrderRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of OrderRepository
 * Demonstrates repository pattern with advanced stream operations and concurrent data structures
 */
public class InMemoryOrderRepository implements OrderRepository {
    
    // Thread-safe concurrent map for storing orders
    private final Map<String, Order> ordersById = new ConcurrentHashMap<>();
    
    @Override
    public Order save(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        
        ordersById.put(order.getOrderId(), order);
        return order;
    }
    
    @Override
    public Optional<Order> findById(String orderId) {
        if (orderId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(ordersById.get(orderId));
    }
    
    @Override
    public List<Order> findByStudentId(String studentId) {
        if (studentId == null) {
            return new ArrayList<>();
        }
        
        return ordersById.values().stream()
                .filter(order -> studentId.equals(order.getStudentId()))
                .sorted(Comparator.comparing(Order::getOrderTime).reversed()) // Most recent first
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Order> findByStatus(OrderStatus status) {
        if (status == null) {
            return new ArrayList<>();
        }
        
        return ordersById.values().stream()
                .filter(order -> order.getStatus() == status)
                .sorted(Comparator.comparing(Order::getOrderTime)) // Oldest first for processing
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Order> findByDate(LocalDate date) {
        if (date == null) {
            return new ArrayList<>();
        }
        
        return ordersById.values().stream()
                .filter(order -> order.getOrderTime().toLocalDate().equals(date))
                .sorted(Comparator.comparing(Order::getOrderTime))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Order> findByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return new ArrayList<>();
        }
        
        return ordersById.values().stream()
                .filter(order -> {
                    LocalDate orderDate = order.getOrderTime().toLocalDate();
                    return !orderDate.isBefore(startDate) && !orderDate.isAfter(endDate);
                })
                .sorted(Comparator.comparing(Order::getOrderTime))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Order> findRecentByStudentId(String studentId, int limit) {
        if (studentId == null || limit <= 0) {
            return new ArrayList<>();
        }
        
        return ordersById.values().stream()
                .filter(order -> studentId.equals(order.getStudentId()))
                .sorted(Comparator.comparing(Order::getOrderTime).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Order> findPendingOrders() {
        return findByStatus(OrderStatus.PENDING);
    }
    
    @Override
    public List<Order> findOrdersInPreparation() {
        return ordersById.values().stream()
                .filter(order -> order.getStatus() == OrderStatus.CONFIRMED || 
                               order.getStatus() == OrderStatus.PREPARING)
                .sorted(Comparator.comparing(Order::getOrderTime))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Order> findOrdersReadyForPickup() {
        return findByStatus(OrderStatus.READY);
    }
    
    @Override
    public List<Order> findAll() {
        return ordersById.values().stream()
                .sorted(Comparator.comparing(Order::getOrderTime).reversed())
                .collect(Collectors.toList());
    }
    
    @Override
    public Order update(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        
        if (!ordersById.containsKey(order.getOrderId())) {
            throw new IllegalArgumentException("Order not found: " + order.getOrderId());
        }
        
        return save(order); // save method handles both create and update
    }
    
    @Override
    public boolean deleteById(String orderId) {
        if (orderId == null) {
            return false;
        }
        
        return ordersById.remove(orderId) != null;
    }
    
    @Override
    public boolean existsById(String orderId) {
        return orderId != null && ordersById.containsKey(orderId);
    }
    
    @Override
    public long[] getCountByStatus() {
        OrderStatus[] statuses = OrderStatus.values();
        long[] counts = new long[statuses.length];
        
        // Use streams to count orders by status
        Map<OrderStatus, Long> statusCount = ordersById.values().stream()
                .collect(Collectors.groupingBy(
                    Order::getStatus,
                    Collectors.counting()
                ));
        
        // Fill the array with counts
        for (int i = 0; i < statuses.length; i++) {
            counts[i] = statusCount.getOrDefault(statuses[i], 0L);
        }
        
        return counts;
    }
    
    /**
     * Additional utility methods for enhanced functionality
     */
    
    public int getOrderCount() {
        return ordersById.size();
    }
    
    public void clear() {
        ordersById.clear();
    }
    
    /**
     * Get total sales for a date range using streams
     */
    public java.math.BigDecimal getTotalSales(LocalDate startDate, LocalDate endDate) {
        return ordersById.values().stream()
                .filter(order -> {
                    LocalDate orderDate = order.getOrderTime().toLocalDate();
                    return !orderDate.isBefore(startDate) && !orderDate.isAfter(endDate);
                })
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .map(Order::getTotalAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
    
    /**
     * Get average order value for a date range
     */
    public OptionalDouble getAverageOrderValue(LocalDate startDate, LocalDate endDate) {
        return ordersById.values().stream()
                .filter(order -> {
                    LocalDate orderDate = order.getOrderTime().toLocalDate();
                    return !orderDate.isBefore(startDate) && !orderDate.isAfter(endDate);
                })
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .mapToDouble(order -> order.getTotalAmount().doubleValue())
                .average();
    }
    
    /**
     * Get top customers by order count
     */
    public List<Map.Entry<String, Long>> getTopCustomersByOrderCount(int limit) {
        return ordersById.values().stream()
                .collect(Collectors.groupingBy(
                    Order::getStudentId,
                    Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Get top customers by total spending
     */
    public List<Map.Entry<String, java.math.BigDecimal>> getTopCustomersBySpending(int limit) {
        return ordersById.values().stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .collect(Collectors.groupingBy(
                    Order::getStudentId,
                    Collectors.mapping(
                        Order::getTotalAmount,
                        Collectors.reducing(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                    )
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, java.math.BigDecimal>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Get orders with specific items
     */
    public List<Order> findOrdersContainingItem(String itemId) {
        if (itemId == null) {
            return new ArrayList<>();
        }
        
        return ordersById.values().stream()
                .filter(order -> order.getItems().stream()
                    .anyMatch(orderItem -> itemId.equals(orderItem.getMenuItem().getItemId())))
                .sorted(Comparator.comparing(Order::getOrderTime).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * Get order statistics by hour of day
     */
    public Map<Integer, Long> getOrderCountByHour() {
        return ordersById.values().stream()
                .collect(Collectors.groupingBy(
                    order -> order.getOrderTime().getHour(),
                    Collectors.counting()
                ));
    }
    
    /**
     * Get order statistics by day of week
     */
    public Map<java.time.DayOfWeek, Long> getOrderCountByDayOfWeek() {
        return ordersById.values().stream()
                .collect(Collectors.groupingBy(
                    order -> order.getOrderTime().getDayOfWeek(),
                    Collectors.counting()
                ));
    }
    
    /**
     * Find orders that used loyalty points
     */
    public List<Order> findOrdersWithLoyaltyRedemption() {
        return ordersById.values().stream()
                .filter(order -> order.getLoyaltyPointsRedeemed() > 0)
                .sorted(Comparator.comparing(Order::getOrderTime).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * Get total loyalty points earned across all orders
     */
    public int getTotalLoyaltyPointsEarned() {
        return ordersById.values().stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .mapToInt(Order::getLoyaltyPointsEarned)
                .sum();
    }
    
    /**
     * Get total loyalty points redeemed across all orders
     */
    public int getTotalLoyaltyPointsRedeemed() {
        return ordersById.values().stream()
                .mapToInt(Order::getLoyaltyPointsRedeemed)
                .sum();
    }
}