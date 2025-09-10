package com.university.cafeteria.ui;

import com.university.cafeteria.model.*;
import com.university.cafeteria.model.MenuItem.MenuCategory;
import com.university.cafeteria.model.Order.OrderStatus;
import com.university.cafeteria.service.CafeteriaSystem;
import com.university.cafeteria.service.LoyaltyService.RedemptionOption;
import com.university.cafeteria.service.NotificationService.Notification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.IntStream;

/**
 * Student interface for the console application
 * Demonstrates user experience design and complex business logic integration
 */
public class StudentInterface {
    
    private final CafeteriaSystem cafeteriaSystem;
    private final Scanner scanner;
    private boolean shouldLogout = false;
    private Order currentOrder;
    
    public StudentInterface(CafeteriaSystem cafeteriaSystem, Scanner scanner) {
        this.cafeteriaSystem = cafeteriaSystem;
        this.scanner = scanner;
    }
    
    public void showStudentDashboard(Student student) {
        shouldLogout = false;
        
        while (!shouldLogout) {
            printStudentHeader(student);
            showStudentMenu();
            handleStudentChoice(student);
        }
    }
    
    private void printStudentHeader(Student student) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║                     🎓 STUDENT DASHBOARD                         ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ Welcome, %-20s                                   ║%n", student.getName());
        System.out.printf("║ Student ID: %-15s  Loyalty Points: %-8d        ║%n", 
                student.getStudentId(), student.getLoyaltyPoints());
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
    }
    
    private void showStudentMenu() {
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("           📱 STUDENT MENU");
        System.out.println("═══════════════════════════════════════");
        System.out.println("1. 🍽️  Browse Menu & Place Order");
        System.out.println("2. 🛒 View Current Order");
        System.out.println("3. 📦 My Order History");
        System.out.println("4. 🏅 Loyalty Points & Rewards");
        System.out.println("5. 🔔 Notifications");
        System.out.println("6. 👤 My Profile");
        System.out.println("7. 🚪 Logout");
        System.out.println("═══════════════════════════════════════");
        System.out.print("Please select an option (1-7): ");
    }
    
    private void handleStudentChoice(Student student) {
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            
            switch (choice) {
                case 1 -> browseMenuAndOrder(student);
                case 2 -> viewCurrentOrder(student);
                case 3 -> viewOrderHistory(student);
                case 4 -> viewLoyaltyProgram(student);
                case 5 -> viewNotifications(student);
                case 6 -> viewProfile(student);
                case 7 -> shouldLogout = true;
                default -> {
                    System.out.println("❌ Invalid option. Please select a number between 1 and 7.");
                    pressEnterToContinue();
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input. Please enter a number between 1 and 7.");
            pressEnterToContinue();
        }
    }
    
    private void browseMenuAndOrder(Student student) {
        System.out.println("\n🍽️ MENU & ORDERING");
        System.out.println("─────────────────────");
        
        while (true) {
            System.out.println("\n1. 📋 View Full Menu");
            System.out.println("2. 🔍 Browse by Category");
            System.out.println("3. 🛒 Create New Order");
            System.out.println("4. ↩️  Back to Dashboard");
            System.out.print("Choose an option: ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                
                switch (choice) {
                    case 1 -> viewFullMenu();
                    case 2 -> browseByCategoryMenu();
                    case 3 -> createNewOrder(student);
                    case 4 -> { return; }
                    default -> System.out.println("❌ Invalid option.");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input.");
            }
        }
    }
    
    private void viewFullMenu() {
        List<MenuItem> items = cafeteriaSystem.getMenuService().getAvailableMenuItems();
        
        if (items.isEmpty()) {
            System.out.println("📭 No menu items available at the moment.");
            pressEnterToContinue();
            return;
        }
        
        System.out.println("\n🍽️ AVAILABLE MENU ITEMS");
        System.out.println("═══════════════════════════════════════════════════════════════════");
        
        MenuCategory currentCategory = null;
        int itemNumber = 1;
        
        for (MenuItem item : items) {
            if (currentCategory != item.getCategory()) {
                currentCategory = item.getCategory();
                System.out.println("\n📂 " + currentCategory.getDisplayName().toUpperCase());
                System.out.println("───────────────────────────────────────────────────────────────────");
            }
            
            System.out.printf("%2d. %-25s %s%n", 
                itemNumber++, item.getName(), item.getFormattedPrice());
            if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                System.out.printf("    📝 %s%n", item.getDescription());
            }
            System.out.println();
        }
        
        pressEnterToContinue();
    }
    
    private void browseByCategoryMenu() {
        System.out.println("\n📂 BROWSE BY CATEGORY");
        System.out.println("──────────────────────");
        
        MenuCategory[] categories = MenuCategory.values();
        for (int i = 0; i < categories.length; i++) {
            System.out.printf("%d. %s%n", i + 1, categories[i].getDisplayName());
        }
        System.out.printf("%d. ↩️  Back%n", categories.length + 1);
        
        System.out.print("Select category: ");
        
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            
            if (choice >= 1 && choice <= categories.length) {
                viewCategoryItems(categories[choice - 1]);
            } else if (choice == categories.length + 1) {
                return;
            } else {
                System.out.println("❌ Invalid category selection.");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input.");
        }
    }
    
    private void viewCategoryItems(MenuCategory category) {
        List<MenuItem> items = cafeteriaSystem.getMenuService()
                .getMenuItemsByCategory(category, true);
        
        if (items.isEmpty()) {
            System.out.println("📭 No items available in " + category.getDisplayName());
            pressEnterToContinue();
            return;
        }
        
        System.out.println("\n🍽️ " + category.getDisplayName().toUpperCase());
        System.out.println("═══════════════════════════════════════════════════════════════");
        
        for (int i = 0; i < items.size(); i++) {
            MenuItem item = items.get(i);
            System.out.printf("%2d. %-25s %s%n", 
                i + 1, item.getName(), item.getFormattedPrice());
            if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                System.out.printf("    📝 %s%n", item.getDescription());
            }
            System.out.println();
        }
        
        pressEnterToContinue();
    }
    
    private void createNewOrder(Student student) {
        if (currentOrder != null && !currentOrder.isEmpty()) {
            System.out.println("⚠️  You already have an active order. Please complete or cancel it first.");
            pressEnterToContinue();
            return;
        }
        
        System.out.println("\n🛒 CREATE NEW ORDER");
        System.out.println("─────────────────────");
        
        currentOrder = cafeteriaSystem.getOrderService().createOrder(student.getStudentId());
        System.out.println("✅ New order created: " + currentOrder.getOrderId());
        
        orderingProcess(student);
    }
    
    private void orderingProcess(Student student) {
        while (true) {
            System.out.println("\n🛒 ORDER MANAGEMENT");
            System.out.println("─────────────────────");
            if (currentOrder != null && !currentOrder.isEmpty()) {
                System.out.println("📦 Current Order: " + currentOrder.getOrderId());
                System.out.println("💰 Total: " + currentOrder.getFormattedTotal());
                System.out.println("📊 Items: " + currentOrder.getTotalItemCount());
            }
            
            System.out.println("\n1. ➕ Add Item to Order");
            System.out.println("2. 📋 View Order Details");
            System.out.println("3. ✏️  Modify Order");
            System.out.println("4. 🏅 Apply Loyalty Discount");
            System.out.println("5. ✅ Confirm Order");
            System.out.println("6. ❌ Cancel Order");
            System.out.println("7. ↩️  Back to Menu");
            System.out.print("Choose an option: ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                
                switch (choice) {
                    case 1 -> addItemToOrder();
                    case 2 -> viewOrderDetails();
                    case 3 -> modifyOrder();
                    case 4 -> applyLoyaltyDiscount(student);
                    case 5 -> { 
                        if (confirmOrder()) return; 
                    }
                    case 6 -> { 
                        if (cancelOrder()) return; 
                    }
                    case 7 -> { return; }
                    default -> System.out.println("❌ Invalid option.");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input.");
            }
        }
    }
    
    private void addItemToOrder() {
        List<MenuItem> items = cafeteriaSystem.getMenuService().getAvailableMenuItems();
        
        if (items.isEmpty()) {
            System.out.println("📭 No menu items available.");
            return;
        }
        
        System.out.println("\n➕ ADD ITEM TO ORDER");
        System.out.println("─────────────────────");
        
        // Display items with numbers
        for (int i = 0; i < items.size(); i++) {
            MenuItem item = items.get(i);
            System.out.printf("%2d. %-25s %s%n", 
                i + 1, item.getName(), item.getFormattedPrice());
        }
        
        System.out.print("\nSelect item number (0 to cancel): ");
        
        try {
            int itemChoice = Integer.parseInt(scanner.nextLine().trim());
            
            if (itemChoice == 0) return;
            
            if (itemChoice < 1 || itemChoice > items.size()) {
                System.out.println("❌ Invalid item selection.");
                return;
            }
            
            MenuItem selectedItem = items.get(itemChoice - 1);
            
            System.out.print("Enter quantity: ");
            int quantity = Integer.parseInt(scanner.nextLine().trim());
            
            if (quantity <= 0) {
                System.out.println("❌ Quantity must be positive.");
                return;
            }
            
            currentOrder = cafeteriaSystem.getOrderService()
                    .addItemToOrder(currentOrder.getOrderId(), selectedItem, quantity);
            
            System.out.printf("✅ Added %dx %s to your order%n", quantity, selectedItem.getName());
            
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input.");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }
    
    private void viewOrderDetails() {
        if (currentOrder == null || currentOrder.isEmpty()) {
            System.out.println("🛒 Your order is empty.");
            return;
        }
        
        System.out.println("\n📋 ORDER DETAILS");
        System.out.println("══════════════════════════════════════════════════════════════");
        System.out.println("Order ID: " + currentOrder.getOrderId());
        System.out.println("Status: " + currentOrder.getStatus().getDisplayName());
        System.out.println("Order Time: " + currentOrder.getOrderTime().toLocalDate() + 
                          " " + currentOrder.getOrderTime().toLocalTime());
        System.out.println("──────────────────────────────────────────────────────────────");
        
        for (Order.OrderItem item : currentOrder.getItems()) {
            System.out.printf("%-25s x%2d  %s%n", 
                item.getMenuItem().getName(),
                item.getQuantity(),
                item.getFormattedSubtotal());
        }
        
        System.out.println("──────────────────────────────────────────────────────────────");
        
        if (currentOrder.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            System.out.println("Loyalty Discount: -" + 
                String.format("EGP %.2f", currentOrder.getDiscountAmount()));
            System.out.println("Points Redeemed: " + currentOrder.getLoyaltyPointsRedeemed());
        }
        
        System.out.println("TOTAL: " + currentOrder.getFormattedTotal());
        System.out.println("Points to Earn: " + currentOrder.getLoyaltyPointsEarned());
        
        pressEnterToContinue();
    }
    
    private void modifyOrder() {
        if (currentOrder == null || currentOrder.isEmpty()) {
            System.out.println("🛒 Your order is empty.");
            return;
        }
        
        System.out.println("\n✏️  MODIFY ORDER");
        System.out.println("─────────────────");
        
        List<Order.OrderItem> items = currentOrder.getItems();
        for (int i = 0; i < items.size(); i++) {
            Order.OrderItem item = items.get(i);
            System.out.printf("%2d. %s x%d - %s%n", 
                i + 1, item.getMenuItem().getName(), 
                item.getQuantity(), item.getFormattedSubtotal());
        }
        
        System.out.print("\nSelect item to modify (0 to cancel): ");
        
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            
            if (choice == 0) return;
            
            if (choice < 1 || choice > items.size()) {
                System.out.println("❌ Invalid selection.");
                return;
            }
            
            Order.OrderItem selectedItem = items.get(choice - 1);
            String itemId = selectedItem.getMenuItem().getItemId();
            
            System.out.printf("Current quantity: %d%n", selectedItem.getQuantity());
            System.out.print("Enter new quantity (0 to remove): ");
            
            int newQuantity = Integer.parseInt(scanner.nextLine().trim());
            
            if (newQuantity < 0) {
                System.out.println("❌ Quantity cannot be negative.");
                return;
            }
            
            if (newQuantity == 0) {
                currentOrder = cafeteriaSystem.getOrderService()
                        .removeItemFromOrder(currentOrder.getOrderId(), itemId);
                System.out.println("✅ Item removed from order.");
            } else {
                currentOrder = cafeteriaSystem.getOrderService()
                        .updateItemQuantity(currentOrder.getOrderId(), itemId, newQuantity);
                System.out.println("✅ Quantity updated.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input.");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }
    
    private void applyLoyaltyDiscount(Student student) {
        if (currentOrder == null || currentOrder.isEmpty()) {
            System.out.println("🛒 Your order is empty.");
            return;
        }
        
        int currentPoints = student.getLoyaltyPoints();
        
        if (currentPoints < 10) {
            System.out.println("❌ You need at least 10 points to redeem rewards.");
            return;
        }
        
        System.out.println("\n🏅 LOYALTY DISCOUNT");
        System.out.println("────────────────────");
        System.out.println("Current Points: " + currentPoints);
        System.out.println("Order Total: " + currentOrder.getFormattedTotal());
        
        List<RedemptionOption> options = cafeteriaSystem.getLoyaltyService()
                .getAvailableRedemptions(currentPoints);
        
        if (options.isEmpty()) {
            System.out.println("❌ No redemption options available with your current points.");
            return;
        }
        
        System.out.println("\nAvailable Redemptions:");
        for (int i = 0; i < options.size(); i++) {
            RedemptionOption option = options.get(i);
            System.out.printf("%d. %s%n", i + 1, option.toString());
        }
        
        System.out.print("\nSelect redemption option (0 to cancel): ");
        
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            
            if (choice == 0) return;
            
            if (choice < 1 || choice > options.size()) {
                System.out.println("❌ Invalid selection.");
                return;
            }
            
            RedemptionOption selectedOption = options.get(choice - 1);
            
            currentOrder = cafeteriaSystem.getOrderService()
                    .applyLoyaltyDiscount(currentOrder.getOrderId(), 
                                        selectedOption.getPointsRequired());
            
            System.out.printf("✅ Applied discount: %d points = %s discount%n",
                selectedOption.getPointsRequired(),
                String.format("EGP %.2f", selectedOption.getDiscountAmount()));
            
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input.");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }
    
    private boolean confirmOrder() {
        if (currentOrder == null || currentOrder.isEmpty()) {
            System.out.println("🛒 Your order is empty.");
            return false;
        }
        
        System.out.println("\n✅ CONFIRM ORDER");
        System.out.println("─────────────────");
        System.out.println("Order Total: " + currentOrder.getFormattedTotal());
        System.out.println("Points to Earn: " + currentOrder.getLoyaltyPointsEarned());
        
        if (currentOrder.getLoyaltyPointsRedeemed() > 0) {
            System.out.println("Points to Redeem: " + currentOrder.getLoyaltyPointsRedeemed());
            System.out.println("Discount Applied: " + 
                String.format("EGP %.2f", currentOrder.getDiscountAmount()));
        }
        
        System.out.print("\nConfirm order? (y/n): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        
        if (confirmation.equals("y") || confirmation.equals("yes")) {
            try {
                currentOrder = cafeteriaSystem.getOrderService()
                        .confirmOrder(currentOrder.getOrderId());
                
                System.out.println("✅ Order confirmed successfully!");
                System.out.println("📧 Confirmation notification sent.");
                System.out.println("⏱️  Estimated preparation time: " + 
                    cafeteriaSystem.getOrderService()
                        .getEstimatedPreparationTime(currentOrder.getOrderId()) + " minutes");
                
                currentOrder = null; // Clear current order
                pressEnterToContinue();
                return true;
                
            } catch (IllegalArgumentException e) {
                System.out.println("❌ " + e.getMessage());
            }
        } else {
            System.out.println("❌ Order confirmation cancelled.");
        }
        
        return false;
    }
    
    private boolean cancelOrder() {
        if (currentOrder == null) {
            return true;
        }
        
        System.out.print("\n❌ Are you sure you want to cancel this order? (y/n): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        
        if (confirmation.equals("y") || confirmation.equals("yes")) {
            try {
                cafeteriaSystem.getOrderService().cancelOrder(currentOrder.getOrderId());
                System.out.println("✅ Order cancelled successfully.");
                currentOrder = null;
                pressEnterToContinue();
                return true;
            } catch (IllegalArgumentException e) {
                System.out.println("❌ " + e.getMessage());
            }
        }
        
        return false;
    }
    
    private void viewCurrentOrder(Student student) {
        // Check for any pending orders
        List<Order> pendingOrders = cafeteriaSystem.getOrderService()
                .getOrdersByStudent(student.getStudentId()).stream()
                .filter(order -> !order.isCompleted())
                .toList();
        
        if (pendingOrders.isEmpty()) {
            System.out.println("\n📭 You have no active orders.");
            pressEnterToContinue();
            return;
        }
        
        System.out.println("\n🛒 YOUR ACTIVE ORDERS");
        System.out.println("═══════════════════════════════════════════════════════════════");
        
        for (Order order : pendingOrders) {
            System.out.println("Order ID: " + order.getOrderId());
            System.out.println("Status: " + order.getStatus().getDisplayName());
            System.out.println("Total: " + order.getFormattedTotal());
            System.out.println("Items: " + order.getTotalItemCount());
            System.out.println("Order Time: " + order.getOrderTime().toLocalDate() + 
                              " " + order.getOrderTime().toLocalTime());
            
            if (order.getStatus() == OrderStatus.PREPARING || order.getStatus() == OrderStatus.CONFIRMED) {
                int estimatedTime = cafeteriaSystem.getOrderService()
                        .getEstimatedPreparationTime(order.getOrderId());
                System.out.println("⏱️  Estimated time remaining: " + estimatedTime + " minutes");
            }
            
            System.out.println("───────────────────────────────────────────────────────────────");
        }
        
        pressEnterToContinue();
    }
    
    private void viewOrderHistory(Student student) {
        List<Order> orders = cafeteriaSystem.getOrderService()
                .getRecentOrdersByStudent(student.getStudentId(), 10);
        
        if (orders.isEmpty()) {
            System.out.println("\n📭 You have no order history.");
            pressEnterToContinue();
            return;
        }
        
        System.out.println("\n📦 YOUR ORDER HISTORY (Last 10 Orders)");
        System.out.println("═══════════════════════════════════════════════════════════════");
        
        for (Order order : orders) {
            System.out.printf("%-12s %-15s %s %2d items %s%n",
                order.getOrderId(),
                order.getStatus().getDisplayName(),
                order.getFormattedTotal(),
                order.getTotalItemCount(),
                order.getOrderTime().toLocalDate());
        }
        
        pressEnterToContinue();
    }
    
    private void viewLoyaltyProgram(Student student) {
        System.out.println("\n🏅 LOYALTY PROGRAM");
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("Current Points: " + student.getLoyaltyPoints());
        
        List<RedemptionOption> options = cafeteriaSystem.getLoyaltyService()
                .getAvailableRedemptions(student.getLoyaltyPoints());
        
        if (!options.isEmpty()) {
            System.out.println("\n🎁 Available Rewards:");
            for (RedemptionOption option : options) {
                System.out.println("  • " + option.toString());
            }
        } else {
            System.out.println("\n💡 Collect more points to unlock rewards!");
            System.out.println("   Next reward at 50 points.");
        }
        
        System.out.println("\n📊 How to Earn Points:");
        System.out.println("  • 1 point for every 10 EGP spent");
        System.out.println("  • Special promotions and bonuses");
        
        System.out.println("\n📈 Recent Transactions:");
        List<LoyaltyAccount.LoyaltyTransaction> recent = cafeteriaSystem.getLoyaltyService()
                .getRecentTransactions(student.getStudentId(), 5);
        
        if (recent.isEmpty()) {
            System.out.println("  No recent transactions");
        } else {
            for (LoyaltyAccount.LoyaltyTransaction transaction : recent) {
                System.out.println("  " + transaction.toString());
            }
        }
        
        pressEnterToContinue();
    }
    
    private void viewNotifications(Student student) {
        List<Notification> notifications = cafeteriaSystem.getNotificationService()
                .getNotificationsForStudent(student.getStudentId());
        
        if (notifications.isEmpty()) {
            System.out.println("\n📭 No notifications.");
            pressEnterToContinue();
            return;
        }
        
        System.out.println("\n🔔 YOUR NOTIFICATIONS");
        System.out.println("═══════════════════════════════════════════════════════════════");
        
        for (int i = 0; i < Math.min(notifications.size(), 10); i++) {
            Notification notification = notifications.get(i);
            String readStatus = notification.isRead() ? "✅" : "🔴";
            
            System.out.printf("%s %s%n", readStatus, notification.getTitle());
            System.out.printf("   %s%n", notification.getMessage());
            System.out.printf("   %s%n", notification.getTimestamp().toLocalDate());
            System.out.println();
        }
        
        // Mark all as read
        int markedCount = cafeteriaSystem.getNotificationService()
                .markAllNotificationsAsRead(student.getStudentId());
        
        if (markedCount > 0) {
            System.out.println("✅ " + markedCount + " notifications marked as read.");
        }
        
        pressEnterToContinue();
    }
    
    private void viewProfile(Student student) {
        System.out.println("\n👤 YOUR PROFILE");
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("Name: " + student.getName());
        System.out.println("User ID: " + student.getUserId());
        System.out.println("Student ID: " + student.getStudentId());
        System.out.println("Account Created: " + student.getCreatedAt().toLocalDate());
        System.out.println("Loyalty Points: " + student.getLoyaltyPoints());
        
        // Calculate some statistics
        List<Order> allOrders = cafeteriaSystem.getOrderService()
                .getOrdersByStudent(student.getStudentId());
        
        long completedOrders = allOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .count();
        
        BigDecimal totalSpent = allOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        System.out.println("\n📊 Your Statistics:");
        System.out.println("Total Orders: " + allOrders.size());
        System.out.println("Completed Orders: " + completedOrders);
        System.out.println("Total Spent: EGP " + String.format("%.2f", totalSpent));
        
        if (completedOrders > 0) {
            BigDecimal avgOrder = totalSpent.divide(BigDecimal.valueOf(completedOrders), 2, BigDecimal.ROUND_HALF_UP);
            System.out.println("Average Order: EGP " + String.format("%.2f", avgOrder));
        }
        
        pressEnterToContinue();
    }
    
    private void pressEnterToContinue() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    public boolean shouldLogout() {
        return shouldLogout;
    }
}