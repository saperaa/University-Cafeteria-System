package com.university.cafeteria.service;

import com.university.cafeteria.model.Order;
import com.university.cafeteria.model.Student;
import java.util.List;

/**
 * Interface for notification services
 * Demonstrates Single Responsibility Principle - focused on notifications only
 */
public interface NotificationService {
    
    /**
     * Send order status notification to student
     * @param order Order with updated status
     * @param message Custom message (optional)
     */
    void notifyOrderStatusUpdate(Order order, String message);
    
    /**
     * Send order ready notification to student
     * @param order Order that is ready for pickup
     */
    void notifyOrderReady(Order order);
    
    /**
     * Send order confirmation notification to student
     * @param order Confirmed order
     */
    void notifyOrderConfirmed(Order order);
    
    /**
     * Send loyalty points earned notification
     * @param studentId Student ID
     * @param pointsEarned Points earned
     * @param orderId Order ID that earned the points
     */
    void notifyPointsEarned(String studentId, int pointsEarned, String orderId);
    
    /**
     * Send loyalty points redeemed notification
     * @param studentId Student ID
     * @param pointsRedeemed Points redeemed
     * @param discountAmount Discount amount received
     */
    void notifyPointsRedeemed(String studentId, int pointsRedeemed, java.math.BigDecimal discountAmount);
    
    /**
     * Send welcome notification to new students
     * @param student Newly registered student
     */
    void notifyStudentWelcome(Student student);
    
    /**
     * Get notifications for a specific student
     * @param studentId Student ID
     * @return List of notifications for the student
     */
    List<Notification> getNotificationsForStudent(String studentId);
    
    /**
     * Get unread notifications for a specific student
     * @param studentId Student ID
     * @return List of unread notifications
     */
    List<Notification> getUnreadNotificationsForStudent(String studentId);
    
    /**
     * Mark notification as read
     * @param notificationId Notification ID
     * @param studentId Student ID (for verification)
     * @return true if marked as read successfully
     */
    boolean markNotificationAsRead(String notificationId, String studentId);
    
    /**
     * Mark all notifications as read for a student
     * @param studentId Student ID
     * @return Number of notifications marked as read
     */
    int markAllNotificationsAsRead(String studentId);
    
    /**
     * Clear old notifications for a student
     * @param studentId Student ID
     * @param daysOld Clear notifications older than this many days
     * @return Number of notifications cleared
     */
    int clearOldNotifications(String studentId, int daysOld);
    
    /**
     * Inner class representing a notification
     */
    class Notification {
        private final String notificationId;
        private final String studentId;
        private final String title;
        private final String message;
        private final NotificationType type;
        private final java.time.LocalDateTime timestamp;
        private boolean read;
        
        public enum NotificationType {
            ORDER_STATUS, ORDER_READY, ORDER_CONFIRMED, 
            POINTS_EARNED, POINTS_REDEEMED, WELCOME, 
            GENERAL
        }
        
        public Notification(String notificationId, String studentId, String title, 
                          String message, NotificationType type) {
            this.notificationId = notificationId;
            this.studentId = studentId;
            this.title = title;
            this.message = message;
            this.type = type;
            this.timestamp = java.time.LocalDateTime.now();
            this.read = false;
        }
        
        // Getters
        public String getNotificationId() { return notificationId; }
        public String getStudentId() { return studentId; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public NotificationType getType() { return type; }
        public java.time.LocalDateTime getTimestamp() { return timestamp; }
        public boolean isRead() { return read; }
        
        public void markAsRead() { this.read = true; }
        
        @Override
        public String toString() {
            return String.format("%s: %s - %s (%s)", 
                type, title, message, timestamp.toLocalDate());
        }
    }
}