package com.university.cafeteria.service.impl;

import com.university.cafeteria.model.Order;
import com.university.cafeteria.model.Order.OrderStatus;
import com.university.cafeteria.model.MenuItem;
import com.university.cafeteria.repository.OrderRepository;
import com.university.cafeteria.service.OrderService;
import com.university.cafeteria.service.LoyaltyService;
import com.university.cafeteria.service.NotificationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Implementation of OrderService
 * Demonstrates Single Responsibility Principle and extensive business logic integration
 * Uses Dependency Injection for OrderRepository, LoyaltyService, and NotificationService
 */
public class OrderServiceImpl implements OrderService {
    
    private final OrderRepository orderRepository;
    private final LoyaltyService loyaltyService;
    private final NotificationService notificationService;
    
    // Business rules configuration
    private static final int BASE_PREPARATION_TIME_MINUTES = 15;
    private static final int ADDITIONAL_TIME_PER_ITEM = 2;
    private static final int MAX_ITEMS_PER_ORDER = 50;
    
    public OrderServiceImpl(OrderRepository orderRepository, 
                           LoyaltyService loyaltyService,
                           NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.loyaltyService = loyaltyService;
        this.notificationService = notificationService;
    }
    
    @Override
    public Order createOrder(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be null or empty");
        }
        
        Order order = new Order(studentId.trim());
        Order saved = orderRepository.save(order);
        persistOrderToDatabase(saved);
        return saved;
    }
    
    @Override
    public Order addItemToOrder(String orderId, MenuItem menuItem, int quantity) {
        validateOrderModification(orderId, menuItem, quantity);
        
        Order order = getOrderByIdOrThrow(orderId);
        validateOrderCanBeModified(order);
        
        // Check if adding this quantity would exceed max items
        if (order.getTotalItemCount() + quantity > MAX_ITEMS_PER_ORDER) {
            throw new IllegalArgumentException("Cannot add items: would exceed maximum of " + MAX_ITEMS_PER_ORDER + " items per order");
        }
        
        order.addItem(menuItem, quantity);
        Order updated = orderRepository.update(order);
        persistOrderToDatabase(updated);
        return updated;
    }
    
    @Override
    public Order removeItemFromOrder(String orderId, String itemId) {
        if (orderId == null || itemId == null) {
            throw new IllegalArgumentException("Order ID and Item ID cannot be null");
        }
        
        Order order = getOrderByIdOrThrow(orderId);
        validateOrderCanBeModified(order);
        
        order.removeItem(itemId);
        Order updated = orderRepository.update(order);
        persistOrderToDatabase(updated);
        return updated;
    }
    
    @Override
    public Order updateItemQuantity(String orderId, String itemId, int newQuantity) {
        if (orderId == null || itemId == null) {
            throw new IllegalArgumentException("Order ID and Item ID cannot be null");
        }
        
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        
        Order order = getOrderByIdOrThrow(orderId);
        validateOrderCanBeModified(order);
        
        // If quantity is 0, remove the item
        if (newQuantity == 0) {
            order.removeItem(itemId);
        } else {
            // Check if updating to this quantity would exceed max items
            int currentQuantity = order.getItems().stream()
                    .filter(item -> item.getMenuItem().getItemId().equals(itemId))
                    .mapToInt(item -> item.getQuantity())
                    .findFirst()
                    .orElse(0);
            
            int totalItemsAfterUpdate = order.getTotalItemCount() - currentQuantity + newQuantity;
            if (totalItemsAfterUpdate > MAX_ITEMS_PER_ORDER) {
                throw new IllegalArgumentException("Cannot update quantity: would exceed maximum of " + MAX_ITEMS_PER_ORDER + " items per order");
            }
            
            order.updateItemQuantity(itemId, newQuantity);
        }
        
        Order updated = orderRepository.update(order);
        persistOrderToDatabase(updated);
        return updated;
    }
    
    @Override
    public Order applyLoyaltyDiscount(String orderId, int pointsToRedeem) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        
        if (pointsToRedeem <= 0) {
            throw new IllegalArgumentException("Points to redeem must be positive");
        }
        
        Order order = getOrderByIdOrThrow(orderId);
        validateOrderCanBeModified(order);
        
        if (order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot apply discount to empty order");
        }
        
        // Check if student can redeem these points
        if (!loyaltyService.canRedeemPoints(order.getStudentId(), pointsToRedeem)) {
            throw new IllegalArgumentException("Student does not have enough loyalty points");
        }
        
        // Calculate discount amount
        BigDecimal discountAmount = loyaltyService.calculateDiscountFromPoints(pointsToRedeem);
        
        // Apply discount to order
        order.applyLoyaltyDiscount(pointsToRedeem, discountAmount);
        
        Order updated = orderRepository.update(order);
        persistOrderToDatabase(updated);
        return updated;
    }
    
    @Override
    public Order confirmOrder(String orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        
        Order order = getOrderByIdOrThrow(orderId);
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Only pending orders can be confirmed");
        }
        
        if (order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot confirm empty order");
        }
        
        // Update order status
        order.updateStatus(OrderStatus.CONFIRMED);
        
        // Redeem loyalty points if discount was applied
        // BUT skip if it's a free water redemption (points already redeemed)
        if (order.getLoyaltyPointsRedeemed() > 0) {
            // Check if this is a free water redemption (100 points, 0 discount)
            if (order.getLoyaltyPointsRedeemed() == 100 && order.getDiscountAmount().compareTo(BigDecimal.ZERO) == 0) {
                // Free water redemption - points already redeemed, skip
                System.out.println("💧 Free water redemption: Skipping point redemption (already done)");
            } else {
                // Regular discount redemption - redeem points now
                loyaltyService.redeemPointsForDiscount(
                    order.getStudentId(), 
                    order.getLoyaltyPointsRedeemed(), 
                    "Redeemed for order " + order.getOrderId()
                );
            }
        }
        
        // Award loyalty points for the order
        loyaltyService.awardPointsFromOrder(
            order.getStudentId(), 
            order.getTotalAmount(), 
            order.getOrderId()
        );
        
        Order updatedOrder = orderRepository.update(order);
        
        // Send confirmation notification
        notificationService.notifyOrderConfirmed(updatedOrder);
        persistOrderToDatabase(updatedOrder);
        
        return updatedOrder;
    }
    
    @Override
    public Order cancelOrder(String orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        
        Order order = getOrderByIdOrThrow(orderId);
        
        if (!order.canBeCancelled()) {
            throw new IllegalArgumentException("Order cannot be cancelled in current status: " + order.getStatus());
        }
        
        order.updateStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.update(order);
        
        // Send cancellation notification
        notificationService.notifyOrderStatusUpdate(updatedOrder, "Your order has been cancelled");
        persistOrderToDatabase(updatedOrder);
        
        return updatedOrder;
    }
    
    @Override
    public Order updateOrderStatus(String orderId, OrderStatus newStatus) {
        if (orderId == null || newStatus == null) {
            throw new IllegalArgumentException("Order ID and new status cannot be null");
        }
        
        Order order = getOrderByIdOrThrow(orderId);
        validateStatusTransition(order.getStatus(), newStatus);
        
        OrderStatus oldStatus = order.getStatus();
        order.updateStatus(newStatus);
        Order updatedOrder = orderRepository.update(order);
        
        // Send appropriate notifications based on status
        handleStatusChangeNotification(updatedOrder, oldStatus, newStatus);
        persistOrderToDatabase(updatedOrder);
        
        return updatedOrder;
    }
    
    @Override
    public Optional<Order> getOrderById(String orderId) {
        if (orderId == null) {
            return Optional.empty();
        }
        return orderRepository.findById(orderId.trim());
    }
    
    @Override
    public List<Order> getOrdersByStudent(String studentId) {
        if (studentId == null) {
            return Collections.emptyList();
        }
        return orderRepository.findByStudentId(studentId.trim());
    }
    
    @Override
    public List<Order> getRecentOrdersByStudent(String studentId, int limit) {
        if (studentId == null || limit <= 0) {
            return Collections.emptyList();
        }
        return orderRepository.findRecentByStudentId(studentId.trim(), limit);
    }
    
    @Override
    public List<Order> getOrdersByStatus(OrderStatus status) {
        if (status == null) {
            return Collections.emptyList();
        }
        return orderRepository.findByStatus(status);
    }
    
    @Override
    public List<Order> getPendingOrders() {
        return orderRepository.findPendingOrders();
    }
    
    @Override
    public List<Order> getOrdersInPreparation() {
        return orderRepository.findOrdersInPreparation();
    }
    
    @Override
    public List<Order> getOrdersReadyForPickup() {
        return orderRepository.findOrdersReadyForPickup();
    }
    
    @Override
    public List<Order> getOrdersByDate(LocalDate date) {
        if (date == null) {
            return Collections.emptyList();
        }
        return orderRepository.findByDate(date);
    }
    
    @Override
    public List<Order> getOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return Collections.emptyList();
        }
        return orderRepository.findByDateRange(startDate, endDate);
    }
    
    @Override
    public int getEstimatedPreparationTime(String orderId) {
        Optional<Order> orderOpt = getOrderById(orderId);
        if (!orderOpt.isPresent()) {
            return 0;
        }
        
        Order order = orderOpt.get();
        int totalItems = order.getTotalItemCount();
        
        return BASE_PREPARATION_TIME_MINUTES + (totalItems * ADDITIONAL_TIME_PER_ITEM);
    }
    
    @Override
    public long[] getOrderStatistics() {
        return orderRepository.getCountByStatus();
    }
    
    /**
     * Helper methods
     */
    
    private Order getOrderByIdOrThrow(String orderId) {
        return getOrderById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }
    
    private void validateOrderModification(String orderId, MenuItem menuItem, int quantity) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        
        if (menuItem == null) {
            throw new IllegalArgumentException("Menu item cannot be null");
        }
        
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        if (!menuItem.isAvailable()) {
            throw new IllegalArgumentException("Menu item is not available: " + menuItem.getName());
        }
    }
    
    private void validateOrderCanBeModified(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Order can only be modified when in PENDING status");
        }
    }
    
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Define valid status transitions
        switch (currentStatus) {
            case PENDING:
                if (newStatus != OrderStatus.CONFIRMED && newStatus != OrderStatus.CANCELLED) {
                    throw new IllegalArgumentException("Invalid status transition from PENDING to " + newStatus);
                }
                break;
            case CONFIRMED:
                if (newStatus != OrderStatus.PREPARING && newStatus != OrderStatus.CANCELLED) {
                    throw new IllegalArgumentException("Invalid status transition from CONFIRMED to " + newStatus);
                }
                break;
            case PREPARING:
                if (newStatus != OrderStatus.READY) {
                    throw new IllegalArgumentException("Invalid status transition from PREPARING to " + newStatus);
                }
                break;
            case READY:
                if (newStatus != OrderStatus.COMPLETED) {
                    throw new IllegalArgumentException("Invalid status transition from READY to " + newStatus);
                }
                break;
            case COMPLETED:
            case CANCELLED:
                throw new IllegalArgumentException("Cannot change status from " + currentStatus);
            default:
                throw new IllegalArgumentException("Unknown status: " + currentStatus);
        }
    }
    
    private void handleStatusChangeNotification(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        switch (newStatus) {
            case CONFIRMED:
                notificationService.notifyOrderConfirmed(order);
                break;
            case PREPARING:
                notificationService.notifyOrderStatusUpdate(order, "Your order is now being prepared");
                break;
            case READY:
                notificationService.notifyOrderReady(order);
                break;
            case COMPLETED:
                notificationService.notifyOrderStatusUpdate(order, "Thank you! Your order has been completed");
                break;
            case CANCELLED:
                notificationService.notifyOrderStatusUpdate(order, "Your order has been cancelled");
                break;
            default:
                notificationService.notifyOrderStatusUpdate(order, null);
                break;
        }
    }
    


    /**
     * --- Minimal JDBC write-through for orders and order items ---
     */
    private void persistOrderToDatabase(Order order) {
        String url = System.getProperty("cafeteria.db.url");
        String user = System.getProperty("cafeteria.db.user");
        String pass = System.getProperty("cafeteria.db.password");
        if (url == null || url.isEmpty()) return; // DB not configured
        boolean mysql = url.startsWith("jdbc:mysql:");

        String upsertOrder = mysql
                ? "INSERT INTO orders (order_id, student_id, status, total_amount, points_earned, points_redeemed, discount_amount, order_time, status_updated_time, notes) VALUES (?,?,?,?,?,?,?,?,?,?) " +
                  "ON DUPLICATE KEY UPDATE student_id=VALUES(student_id), status=VALUES(status), total_amount=VALUES(total_amount), points_earned=VALUES(points_earned), points_redeemed=VALUES(points_redeemed), discount_amount=VALUES(discount_amount), order_time=VALUES(order_time), status_updated_time=VALUES(status_updated_time), notes=VALUES(notes)"
                : "MERGE INTO orders (order_id, student_id, status, total_amount, points_earned, points_redeemed, discount_amount, order_time, status_updated_time, notes) KEY(order_id) VALUES (?,?,?,?,?,?,?,?,?,?)";

        String deleteItems = "DELETE FROM order_items WHERE order_id = ?";
        String insertItem = "INSERT INTO order_items (order_id, item_id, quantity, subtotal) VALUES (?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            try (PreparedStatement ps = conn.prepareStatement(upsertOrder)) {
                ps.setString(1, order.getOrderId());
                ps.setString(2, order.getStudentId());
                ps.setString(3, order.getStatus().name());
                ps.setBigDecimal(4, order.getTotalAmount());
                ps.setInt(5, order.getLoyaltyPointsEarned());
                ps.setInt(6, order.getLoyaltyPointsRedeemed());
                ps.setBigDecimal(7, order.getDiscountAmount());
                ps.setTimestamp(8, Timestamp.valueOf(order.getOrderTime()));
                ps.setTimestamp(9, Timestamp.valueOf(order.getStatusUpdatedTime()));
                ps.setString(10, order.getNotes());
                ps.executeUpdate();
            }
            try (PreparedStatement del = conn.prepareStatement(deleteItems)) {
                del.setString(1, order.getOrderId());
                del.executeUpdate();
            }
            for (Order.OrderItem oi : order.getItems()) {
                try (PreparedStatement ins = conn.prepareStatement(insertItem)) {
                    ins.setString(1, order.getOrderId());
                    ins.setString(2, oi.getMenuItem().getItemId());
                    ins.setInt(3, oi.getQuantity());
                    ins.setBigDecimal(4, oi.getSubtotal());
                    ins.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] Failed to persist order '" + order.getOrderId() + "': " + e.getMessage());
        }
    }
}