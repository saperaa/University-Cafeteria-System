package com.university.cafeteria.service.impl;

import com.university.cafeteria.model.Order;
import com.university.cafeteria.model.Student;
import com.university.cafeteria.service.NotificationService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of NotificationService
 * Demonstrates Single Responsibility Principle - handles only notification logic
 * Uses concurrent data structures for thread safety and streams for data processing
 */
public class NotificationServiceImpl implements NotificationService {
    
    // Thread-safe storage for notifications
    private final Map<String, List<Notification>> notificationsByStudent = new ConcurrentHashMap<>();
    private final Map<String, Notification> notificationsById = new ConcurrentHashMap<>();
    
    // Configuration
    private static final int MAX_NOTIFICATIONS_PER_STUDENT = 100;
    private static final int DEFAULT_NOTIFICATION_RETENTION_DAYS = 30;
    
    @Override
    public void notifyOrderStatusUpdate(Order order, String message) {
        if (order == null) {
            return;
        }
        
        String title = "Order Status Update";
        String notificationMessage = message != null ? message : 
            String.format("Your order %s is now %s", order.getOrderId(), order.getStatus().getDisplayName());
        
        createNotification(order.getStudentId(), title, notificationMessage, Notification.NotificationType.ORDER_STATUS);
        
        // Also log to console for immediate feedback
        System.out.println(String.format("[NOTIFICATION] Student %s: %s", order.getStudentId(), notificationMessage));
    }
    
    @Override
    public void notifyOrderReady(Order order) {
        if (order == null) {
            return;
        }
        
        String title = "Order Ready for Pickup!";
        String message = String.format("Your order %s is ready for pickup. Please collect it from the cafeteria counter.", 
                                      order.getOrderId());
        
        createNotification(order.getStudentId(), title, message, Notification.NotificationType.ORDER_READY);
        
        // Console notification for immediate feedback
        System.out.println(String.format("[PICKUP READY] Order %s for student %s is ready!", 
                                        order.getOrderId(), order.getStudentId()));
    }
    
    @Override
    public void notifyOrderConfirmed(Order order) {
        if (order == null) {
            return;
        }
        
        String title = "Order Confirmed";
        String message = String.format("Your order %s has been confirmed. Total: %s. " +
                                      "You earned %d loyalty points!", 
                                      order.getOrderId(), order.getFormattedTotal(), 
                                      order.getLoyaltyPointsEarned());
        
        createNotification(order.getStudentId(), title, message, Notification.NotificationType.ORDER_CONFIRMED);
    }
    
    @Override
    public void notifyPointsEarned(String studentId, int pointsEarned, String orderId) {
        if (studentId == null || pointsEarned <= 0) {
            return;
        }
        
        String title = "Loyalty Points Earned!";
        String message = String.format("You earned %d loyalty points from order %s. " +
                                      "Keep collecting points for great rewards!", 
                                      pointsEarned, orderId);
        
        createNotification(studentId, title, message, Notification.NotificationType.POINTS_EARNED);
    }
    
    @Override
    public void notifyPointsRedeemed(String studentId, int pointsRedeemed, BigDecimal discountAmount) {
        if (studentId == null || pointsRedeemed <= 0) {
            return;
        }
        
        String title = "Loyalty Points Redeemed";
        String message = String.format("You redeemed %d loyalty points for EGP %.2f discount. " +
                                      "Thank you for your loyalty!", 
                                      pointsRedeemed, discountAmount);
        
        createNotification(studentId, title, message, Notification.NotificationType.POINTS_REDEEMED);
    }
    
    @Override
    public void notifyStudentWelcome(Student student) {
        if (student == null) {
            return;
        }
        
        String title = "Welcome to the Cafeteria System!";
        String message = String.format("Welcome %s! Your account has been created successfully. " +
                                      "Start ordering to earn loyalty points and enjoy great rewards!", 
                                      student.getName());
        
        createNotification(student.getStudentId(), title, message, Notification.NotificationType.WELCOME);
    }
    
    @Override
    public List<Notification> getNotificationsForStudent(String studentId) {
        if (studentId == null) {
            return new ArrayList<>();
        }
        
        return notificationsByStudent.getOrDefault(studentId, new ArrayList<>())
                .stream()
                .sorted((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp())) // Most recent first
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Notification> getUnreadNotificationsForStudent(String studentId) {
        return getNotificationsForStudent(studentId).stream()
                .filter(notification -> !notification.isRead())
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean markNotificationAsRead(String notificationId, String studentId) {
        if (notificationId == null || studentId == null) {
            return false;
        }
        
        Notification notification = notificationsById.get(notificationId);
        if (notification != null && studentId.equals(notification.getStudentId())) {
            notification.markAsRead();
            return true;
        }
        
        return false;
    }
    
    @Override
    public int markAllNotificationsAsRead(String studentId) {
        if (studentId == null) {
            return 0;
        }
        
        List<Notification> notifications = notificationsByStudent.getOrDefault(studentId, new ArrayList<>());
        int markedCount = 0;
        
        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                notification.markAsRead();
                markedCount++;
            }
        }
        
        return markedCount;
    }
    
    @Override
    public int clearOldNotifications(String studentId, int daysOld) {
        if (studentId == null || daysOld <= 0) {
            return 0;
        }
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        List<Notification> notifications = notificationsByStudent.getOrDefault(studentId, new ArrayList<>());
        
        List<Notification> oldNotifications = notifications.stream()
                .filter(notification -> notification.getTimestamp().isBefore(cutoffDate))
                .collect(Collectors.toList());
        
        // Remove old notifications
        notifications.removeAll(oldNotifications);
        oldNotifications.forEach(notification -> notificationsById.remove(notification.getNotificationId()));
        
        return oldNotifications.size();
    }
    
    /**
     * Helper method to create and store notifications
     */
    private void createNotification(String studentId, String title, String message, 
                                   Notification.NotificationType type) {
        if (studentId == null) {
            return;
        }
        
        String notificationId = generateNotificationId();
        Notification notification = new Notification(notificationId, studentId, title, message, type);
        
        // Store in both maps
        notificationsById.put(notificationId, notification);
        
        // Add to student's notification list
        notificationsByStudent.computeIfAbsent(studentId, k -> new ArrayList<>()).add(notification);
        
        // Limit number of notifications per student
        limitNotificationsPerStudent(studentId);
    }
    
    /**
     * Limit notifications per student to prevent memory issues
     */
    private void limitNotificationsPerStudent(String studentId) {
        List<Notification> notifications = notificationsByStudent.get(studentId);
        if (notifications != null && notifications.size() > MAX_NOTIFICATIONS_PER_STUDENT) {
            // Remove oldest notifications
            notifications.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));
            
            while (notifications.size() > MAX_NOTIFICATIONS_PER_STUDENT) {
                Notification removed = notifications.remove(notifications.size() - 1);
                notificationsById.remove(removed.getNotificationId());
            }
        }
    }
    
    /**
     * Generate unique notification ID
     */
    private String generateNotificationId() {
        return "NOTIF_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Additional utility methods
     */
    
    public int getTotalNotificationCount() {
        return notificationsById.size();
    }
    
    public int getNotificationCountForStudent(String studentId) {
        return notificationsByStudent.getOrDefault(studentId, new ArrayList<>()).size();
    }
    
    public int getUnreadNotificationCountForStudent(String studentId) {
        return getUnreadNotificationsForStudent(studentId).size();
    }
    
    /**
     * Get notification statistics
     */
    public NotificationStatistics getNotificationStatistics() {
        int totalNotifications = notificationsById.size();
        int totalStudentsWithNotifications = notificationsByStudent.size();
        
        Map<Notification.NotificationType, Long> typeCount = notificationsById.values().stream()
                .collect(Collectors.groupingBy(
                    Notification::getType,
                    Collectors.counting()
                ));
        
        long unreadCount = notificationsById.values().stream()
                .filter(notification -> !notification.isRead())
                .count();
        
        return new NotificationStatistics(
                totalNotifications,
                totalStudentsWithNotifications,
                (int) unreadCount,
                typeCount
        );
    }
    
    /**
     * Clear all notifications (for testing)
     */
    public void clearAllNotifications() {
        notificationsByStudent.clear();
        notificationsById.clear();
    }
    
    /**
     * Automatically clean up old notifications
     */
    public int performMaintenanceCleanup() {
        int totalCleaned = 0;
        
        for (String studentId : notificationsByStudent.keySet()) {
            totalCleaned += clearOldNotifications(studentId, DEFAULT_NOTIFICATION_RETENTION_DAYS);
        }
        
        return totalCleaned;
    }
    
    /**
     * Send general notification to student
     */
    public void sendGeneralNotification(String studentId, String title, String message) {
        createNotification(studentId, title, message, Notification.NotificationType.GENERAL);
    }
    
    /**
     * Inner class for notification statistics
     */
    public static class NotificationStatistics {
        private final int totalNotifications;
        private final int totalStudentsWithNotifications;
        private final int unreadNotifications;
        private final Map<Notification.NotificationType, Long> notificationsByType;
        
        public NotificationStatistics(int totalNotifications, int totalStudentsWithNotifications,
                                    int unreadNotifications, 
                                    Map<Notification.NotificationType, Long> notificationsByType) {
            this.totalNotifications = totalNotifications;
            this.totalStudentsWithNotifications = totalStudentsWithNotifications;
            this.unreadNotifications = unreadNotifications;
            this.notificationsByType = notificationsByType;
        }
        
        public int getTotalNotifications() { return totalNotifications; }
        public int getTotalStudentsWithNotifications() { return totalStudentsWithNotifications; }
        public int getUnreadNotifications() { return unreadNotifications; }
        public Map<Notification.NotificationType, Long> getNotificationsByType() { return notificationsByType; }
        
        @Override
        public String toString() {
            return String.format("NotificationStatistics{total=%d, students=%d, unread=%d}",
                               totalNotifications, totalStudentsWithNotifications, unreadNotifications);
        }
    }
}