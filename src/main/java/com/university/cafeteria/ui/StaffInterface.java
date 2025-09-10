package com.university.cafeteria.ui;

import com.university.cafeteria.model.*;
import com.university.cafeteria.model.MenuItem.MenuCategory;
import com.university.cafeteria.model.Order.OrderStatus;
import com.university.cafeteria.service.CafeteriaSystem;
import com.university.cafeteria.service.ReportingService.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Staff interface for the console application
 * Demonstrates role-based access control and administrative functionality
 */
public class StaffInterface {
    
    private final CafeteriaSystem cafeteriaSystem;
    private final Scanner scanner;
    private boolean shouldLogout = false;
    
    public StaffInterface(CafeteriaSystem cafeteriaSystem, Scanner scanner) {
        this.cafeteriaSystem = cafeteriaSystem;
        this.scanner = scanner;
    }
    
    public void showStaffDashboard(Staff staff) {
        shouldLogout = false;
        
        while (!shouldLogout) {
            printStaffHeader(staff);
            showStaffMenu(staff);
            handleStaffChoice(staff);
        }
    }
    
    private void printStaffHeader(Staff staff) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║                     👨‍💼 STAFF DASHBOARD                           ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ Welcome, %-20s                                   ║%n", staff.getName());
        System.out.printf("║ Role: %-15s     Employee ID: %-10s        ║%n", 
                staff.getRole().name(), staff.getEmployeeId());
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
    }
    
    private void showStaffMenu(Staff staff) {
        System.out.println("\n═══════════════════════════════════════");
        System.out.println("           💼 STAFF MENU");
        System.out.println("═══════════════════════════════════════");
        System.out.println("1. 📦 Order Management");
        System.out.println("2. 📊 Dashboard & Statistics");
        
        if (staff.canManageMenu()) {
            System.out.println("3. 🍽️  Menu Management");
        }
        
        if (staff.canViewReports()) {
            System.out.println("4. 📈 Reports & Analytics");
        }
        
        System.out.println("5. 👥 User Management");
        System.out.println("6. 🔔 System Notifications");
        System.out.println("7. 🚪 Logout");
        System.out.println("═══════════════════════════════════════");
        System.out.print("Please select an option: ");
    }
    
    private void handleStaffChoice(Staff staff) {
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            
            switch (choice) {
                case 1 -> orderManagement(staff);
                case 2 -> showDashboardStatistics();
                case 3 -> {
                    if (staff.canManageMenu()) {
                        menuManagement();
                    } else {
                        System.out.println("❌ Access denied. Insufficient permissions.");
                        pressEnterToContinue();
                    }
                }
                case 4 -> {
                    if (staff.canViewReports()) {
                        reportsAndAnalytics();
                    } else {
                        System.out.println("❌ Access denied. Insufficient permissions.");
                        pressEnterToContinue();
                    }
                }
                case 5 -> userManagement();
                case 6 -> systemNotifications();
                case 7 -> shouldLogout = true;
                default -> {
                    System.out.println("❌ Invalid option.");
                    pressEnterToContinue();
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input. Please enter a number.");
            pressEnterToContinue();
        }
    }
    
    private void orderManagement(Staff staff) {
        while (true) {
            System.out.println("\n📦 ORDER MANAGEMENT");
            System.out.println("─────────────────────");
            System.out.println("1. 📋 View Pending Orders");
            System.out.println("2. 🔄 View Orders in Preparation");
            System.out.println("3. ✅ View Orders Ready for Pickup");
            System.out.println("4. 🔍 Search Order by ID");
            System.out.println("5. 📈 Update Order Status");
            System.out.println("6. ↩️  Back to Dashboard");
            System.out.print("Choose an option: ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                
                switch (choice) {
                    case 1 -> viewPendingOrders();
                    case 2 -> viewOrdersInPreparation();
                    case 3 -> viewOrdersReady();
                    case 4 -> searchOrderById();
                    case 5 -> updateOrderStatus();
                    case 6 -> { return; }
                    default -> System.out.println("❌ Invalid option.");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input.");
            }
        }
    }
    
    private void viewPendingOrders() {
        List<Order> pendingOrders = cafeteriaSystem.getOrderService().getPendingOrders();
        
        if (pendingOrders.isEmpty()) {
            System.out.println("\n✅ No pending orders!");
            pressEnterToContinue();
            return;
        }
        
        System.out.println("\n📋 PENDING ORDERS");
        System.out.println("═══════════════════════════════════════════════════════════════");
        
        for (Order order : pendingOrders) {
            displayOrderSummary(order);
        }
        
        pressEnterToContinue();
    }
    
    private void viewOrdersInPreparation() {
        List<Order> ordersInPrep = cafeteriaSystem.getOrderService().getOrdersInPreparation();
        
        if (ordersInPrep.isEmpty()) {
            System.out.println("\n✅ No orders currently in preparation!");
            pressEnterToContinue();
            return;
        }
        
        System.out.println("\n🔄 ORDERS IN PREPARATION");
        System.out.println("═══════════════════════════════════════════════════════════════");
        
        for (Order order : ordersInPrep) {
            displayOrderSummary(order);
            System.out.println("⏱️  Estimated time: " + 
                cafeteriaSystem.getOrderService().getEstimatedPreparationTime(order.getOrderId()) + " min");
            System.out.println("───────────────────────────────────────────────────────────────");
        }
        
        pressEnterToContinue();
    }
    
    private void viewOrdersReady() {
        List<Order> readyOrders = cafeteriaSystem.getOrderService().getOrdersReadyForPickup();
        
        if (readyOrders.isEmpty()) {
            System.out.println("\n✅ No orders ready for pickup!");
            pressEnterToContinue();
            return;
        }
        
        System.out.println("\n✅ ORDERS READY FOR PICKUP");
        System.out.println("═══════════════════════════════════════════════════════════════");
        
        for (Order order : readyOrders) {
            displayOrderSummary(order);
        }
        
        pressEnterToContinue();
    }
    
    private void searchOrderById() {
        System.out.print("\n🔍 Enter Order ID: ");
        String orderId = scanner.nextLine().trim();
        
        if (orderId.isEmpty()) {
            System.out.println("❌ Order ID cannot be empty.");
            return;
        }
        
        var orderOpt = cafeteriaSystem.getOrderService().getOrderById(orderId);
        
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            System.out.println("\n📦 ORDER DETAILS");
            System.out.println("═══════════════════════════════════════════════════════════════");
            displayDetailedOrder(order);
        } else {
            System.out.println("❌ Order not found: " + orderId);
        }
        
        pressEnterToContinue();
    }
    
    private void updateOrderStatus() {
        System.out.print("\n📈 Enter Order ID to update: ");
        String orderId = scanner.nextLine().trim();
        
        if (orderId.isEmpty()) {
            System.out.println("❌ Order ID cannot be empty.");
            return;
        }
        
        var orderOpt = cafeteriaSystem.getOrderService().getOrderById(orderId);
        
        if (orderOpt.isEmpty()) {
            System.out.println("❌ Order not found: " + orderId);
            return;
        }
        
        Order order = orderOpt.get();
        
        System.out.println("\nCurrent Status: " + order.getStatus().getDisplayName());
        System.out.println("\nAvailable Status Updates:");
        
        OrderStatus currentStatus = order.getStatus();
        
        switch (currentStatus) {
            case PENDING -> {
                System.out.println("1. Confirm Order");
                System.out.println("2. Cancel Order");
            }
            case CONFIRMED -> {
                System.out.println("1. Start Preparation");
                System.out.println("2. Cancel Order");
            }
            case PREPARING -> {
                System.out.println("1. Mark as Ready");
            }
            case READY -> {
                System.out.println("1. Mark as Completed");
            }
            default -> {
                System.out.println("❌ No status updates available for " + currentStatus);
                pressEnterToContinue();
                return;
            }
        }
        
        System.out.print("Select option: ");
        
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            OrderStatus newStatus = null;
            
            switch (currentStatus) {
                case PENDING -> newStatus = (choice == 1) ? OrderStatus.CONFIRMED : 
                                           (choice == 2) ? OrderStatus.CANCELLED : null;
                case CONFIRMED -> newStatus = (choice == 1) ? OrderStatus.PREPARING : 
                                             (choice == 2) ? OrderStatus.CANCELLED : null;
                case PREPARING -> newStatus = (choice == 1) ? OrderStatus.READY : null;
                case READY -> newStatus = (choice == 1) ? OrderStatus.COMPLETED : null;
            }
            
            if (newStatus != null) {
                cafeteriaSystem.getOrderService().updateOrderStatus(orderId, newStatus);
                System.out.println("✅ Order status updated to: " + newStatus.getDisplayName());
            } else {
                System.out.println("❌ Invalid choice.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input.");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
        }
        
        pressEnterToContinue();
    }
    
    private void displayOrderSummary(Order order) {
        System.out.printf("Order: %-12s | Status: %-15s | Total: %s%n",
            order.getOrderId(),
            order.getStatus().getDisplayName(),
            order.getFormattedTotal());
        System.out.printf("Student: %-10s | Items: %2d | Time: %s%n",
            order.getStudentId(),
            order.getTotalItemCount(),
            order.getOrderTime().toLocalTime());
        System.out.println("───────────────────────────────────────────────────────────────");
    }
    
    private void displayDetailedOrder(Order order) {
        System.out.println("Order ID: " + order.getOrderId());
        System.out.println("Student ID: " + order.getStudentId());
        System.out.println("Status: " + order.getStatus().getDisplayName());
        System.out.println("Order Time: " + order.getOrderTime());
        System.out.println("Last Updated: " + order.getStatusUpdatedTime());
        System.out.println();
        
        System.out.println("ITEMS:");
        for (Order.OrderItem item : order.getItems()) {
            System.out.printf("  %-25s x%2d  %s%n",
                item.getMenuItem().getName(),
                item.getQuantity(),
                item.getFormattedSubtotal());
        }
        
        System.out.println();
        if (order.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            System.out.println("Discount Applied: -" + 
                String.format("EGP %.2f", order.getDiscountAmount()));
        }
        System.out.println("TOTAL: " + order.getFormattedTotal());
        
        if (!order.getNotes().isEmpty()) {
            System.out.println("Notes: " + order.getNotes());
        }
    }
    
    private void showDashboardStatistics() {
        System.out.println("\n📊 SYSTEM DASHBOARD");
        System.out.println("═══════════════════════════════════════════════════════════════");
        
        DashboardData dashboard = cafeteriaSystem.getReportingService().getDashboardData();
        
        System.out.println("📦 ORDER STATISTICS:");
        System.out.println("  Pending Orders: " + dashboard.getPendingOrders());
        System.out.println("  In Preparation: " + dashboard.getOrdersInPreparation());
        System.out.println("  Ready for Pickup: " + dashboard.getOrdersReadyForPickup());
        System.out.println();
        
        System.out.println("💰 TODAY'S PERFORMANCE:");
        System.out.println("  Sales: " + String.format("EGP %.2f", dashboard.getTodaysSales()));
        System.out.println("  Orders: " + dashboard.getTodaysOrders());
        System.out.println("  Active Students: " + dashboard.getActiveStudents());
        System.out.println();
        
        CafeteriaSystem.SystemStatistics stats = cafeteriaSystem.getSystemStatistics();
        System.out.println("🏢 SYSTEM OVERVIEW:");
        System.out.println("  Total Users: " + stats.getTotalUsers());
        System.out.println("  Menu Items: " + stats.getAvailableMenuItems() + "/" + stats.getTotalMenuItems());
        System.out.println("  Total Orders: " + stats.getTotalOrders());
        
        System.out.println("\nLast Updated: " + dashboard.getLastUpdated().toLocalTime());
        
        pressEnterToContinue();
    }
    
    private void menuManagement() {
        while (true) {
            System.out.println("\n🍽️  MENU MANAGEMENT");
            System.out.println("─────────────────────");
            System.out.println("1. 📋 View All Menu Items");
            System.out.println("2. ➕ Add New Menu Item");
            System.out.println("3. ✏️  Edit Menu Item");
            System.out.println("4. ❌ Remove Menu Item");
            System.out.println("5. 🔄 Toggle Item Availability");
            System.out.println("6. 📊 Menu Statistics");
            System.out.println("7. ↩️  Back to Dashboard");
            System.out.print("Choose an option: ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                
                switch (choice) {
                    case 1 -> viewAllMenuItems();
                    case 2 -> addNewMenuItem();
                    case 3 -> editMenuItem();
                    case 4 -> removeMenuItem();
                    case 5 -> toggleItemAvailability();
                    case 6 -> showMenuStatistics();
                    case 7 -> { return; }
                    default -> System.out.println("❌ Invalid option.");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input.");
            }
        }
    }
    
    private void viewAllMenuItems() {
        List<MenuItem> items = cafeteriaSystem.getMenuService().getAllMenuItems();
        
        if (items.isEmpty()) {
            System.out.println("\n📭 No menu items found.");
            pressEnterToContinue();
            return;
        }
        
        System.out.println("\n📋 ALL MENU ITEMS");
        System.out.println("═══════════════════════════════════════════════════════════════");
        
        MenuCategory currentCategory = null;
        
        for (MenuItem item : items) {
            if (currentCategory != item.getCategory()) {
                currentCategory = item.getCategory();
                System.out.println("\n📂 " + currentCategory.getDisplayName().toUpperCase());
                System.out.println("───────────────────────────────────────────────────────────────");
            }
            
            String availability = item.isAvailable() ? "✅" : "❌";
            System.out.printf("%s %-25s %s [%s]%n",
                availability,
                item.getName(),
                item.getFormattedPrice(),
                item.getItemId());
            
            if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                System.out.printf("   📝 %s%n", item.getDescription());
            }
            System.out.println();
        }
        
        pressEnterToContinue();
    }
    
    private void addNewMenuItem() {
        System.out.println("\n➕ ADD NEW MENU ITEM");
        System.out.println("─────────────────────");
        
        try {
            System.out.print("Item Name: ");
            String name = scanner.nextLine().trim();
            
            System.out.print("Description: ");
            String description = scanner.nextLine().trim();
            
            System.out.print("Price (EGP): ");
            BigDecimal price = new BigDecimal(scanner.nextLine().trim());
            
            System.out.println("\nCategories:");
            MenuCategory[] categories = MenuCategory.values();
            for (int i = 0; i < categories.length; i++) {
                System.out.printf("%d. %s%n", i + 1, categories[i].getDisplayName());
            }
            
            System.out.print("Select category: ");
            int categoryChoice = Integer.parseInt(scanner.nextLine().trim());
            
            if (categoryChoice < 1 || categoryChoice > categories.length) {
                System.out.println("❌ Invalid category selection.");
                return;
            }
            
            MenuCategory category = categories[categoryChoice - 1];
            
            MenuItem newItem = cafeteriaSystem.getMenuService()
                    .addMenuItem(name, description, price, category);
            
            System.out.println("✅ Menu item added successfully!");
            System.out.println("Item ID: " + newItem.getItemId());
            
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid number format.");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
        }
        
        pressEnterToContinue();
    }
    
    private void editMenuItem() {
        System.out.print("\n✏️  Enter Item ID to edit: ");
        String itemId = scanner.nextLine().trim();
        
        var itemOpt = cafeteriaSystem.getMenuService().getMenuItemById(itemId);
        
        if (itemOpt.isEmpty()) {
            System.out.println("❌ Item not found: " + itemId);
            pressEnterToContinue();
            return;
        }
        
        MenuItem item = itemOpt.get();
        
        System.out.println("\nCurrent Item Details:");
        System.out.println("Name: " + item.getName());
        System.out.println("Description: " + item.getDescription());
        System.out.println("Price: " + item.getFormattedPrice());
        System.out.println("Category: " + item.getCategory().getDisplayName());
        
        try {
            System.out.print("\nNew Name (press Enter to keep current): ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) name = null;
            
            System.out.print("New Description (press Enter to keep current): ");
            String description = scanner.nextLine().trim();
            if (description.isEmpty()) description = null;
            
            System.out.print("New Price (press Enter to keep current): ");
            String priceStr = scanner.nextLine().trim();
            BigDecimal price = priceStr.isEmpty() ? null : new BigDecimal(priceStr);
            
            MenuItem updatedItem = cafeteriaSystem.getMenuService()
                    .updateMenuItem(itemId, name, description, price, null);
            
            System.out.println("✅ Menu item updated successfully!");
            
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid price format.");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
        }
        
        pressEnterToContinue();
    }
    
    private void removeMenuItem() {
        System.out.print("\n❌ Enter Item ID to remove: ");
        String itemId = scanner.nextLine().trim();
        
        var itemOpt = cafeteriaSystem.getMenuService().getMenuItemById(itemId);
        
        if (itemOpt.isEmpty()) {
            System.out.println("❌ Item not found: " + itemId);
            pressEnterToContinue();
            return;
        }
        
        MenuItem item = itemOpt.get();
        System.out.println("Item to remove: " + item.getName() + " - " + item.getFormattedPrice());
        
        System.out.print("Are you sure? (y/n): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
        
        if (confirmation.equals("y") || confirmation.equals("yes")) {
            boolean removed = cafeteriaSystem.getMenuService().removeMenuItem(itemId);
            if (removed) {
                System.out.println("✅ Menu item removed successfully!");
            } else {
                System.out.println("❌ Failed to remove item.");
            }
        } else {
            System.out.println("❌ Removal cancelled.");
        }
        
        pressEnterToContinue();
    }
    
    private void toggleItemAvailability() {
        System.out.print("\n🔄 Enter Item ID to toggle availability: ");
        String itemId = scanner.nextLine().trim();
        
        var itemOpt = cafeteriaSystem.getMenuService().getMenuItemById(itemId);
        
        if (itemOpt.isEmpty()) {
            System.out.println("❌ Item not found: " + itemId);
            pressEnterToContinue();
            return;
        }
        
        MenuItem item = itemOpt.get();
        boolean newAvailability = !item.isAvailable();
        
        boolean updated = cafeteriaSystem.getMenuService()
                .setItemAvailability(itemId, newAvailability);
        
        if (updated) {
            String status = newAvailability ? "available" : "unavailable";
            System.out.printf("✅ %s is now %s%n", item.getName(), status);
        } else {
            System.out.println("❌ Failed to update availability.");
        }
        
        pressEnterToContinue();
    }
    
    private void showMenuStatistics() {
        System.out.println("\n📊 MENU STATISTICS");
        System.out.println("═══════════════════════════════════════════════════════════════");
        
        long[] categoryStats = cafeteriaSystem.getMenuService().getMenuStatistics();
        MenuCategory[] categories = MenuCategory.values();
        
        int totalItems = 0;
        for (int i = 0; i < categories.length; i++) {
            System.out.printf("%-15s: %d items%n", 
                categories[i].getDisplayName(), categoryStats[i]);
            totalItems += categoryStats[i];
        }
        
        System.out.println("───────────────────────────────────────────────────────────────");
        System.out.println("Total Items: " + totalItems);
        
        int availableItems = cafeteriaSystem.getMenuService().getAvailableMenuItems().size();
        System.out.println("Available Items: " + availableItems);
        System.out.println("Unavailable Items: " + (totalItems - availableItems));
        
        pressEnterToContinue();
    }
    
    private void reportsAndAnalytics() {
        while (true) {
            System.out.println("\n📈 REPORTS & ANALYTICS");
            System.out.println("─────────────────────────");
            System.out.println("1. 📅 Daily Sales Report");
            System.out.println("2. 🗓️  Weekly Sales Report");
            System.out.println("3. 🏅 Loyalty Points Report");
            System.out.println("4. 🔥 Top Selling Items");
            System.out.println("5. 👥 Student Activity Report");
            System.out.println("6. ↩️  Back to Dashboard");
            System.out.print("Choose an option: ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                
                switch (choice) {
                    case 1 -> generateDailySalesReport();
                    case 2 -> generateWeeklySalesReport();
                    case 3 -> generateLoyaltyPointsReport();
                    case 4 -> generateTopSellingItemsReport();
                    case 5 -> generateStudentActivityReport();
                    case 6 -> { return; }
                    default -> System.out.println("❌ Invalid option.");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input.");
            }
        }
    }
    
    private void generateDailySalesReport() {
        System.out.print("\n📅 Enter date (YYYY-MM-DD) or press Enter for today: ");
        String dateStr = scanner.nextLine().trim();
        
        LocalDate date = dateStr.isEmpty() ? LocalDate.now() : LocalDate.parse(dateStr);
        
        DailySalesReport report = cafeteriaSystem.getReportingService()
                .generateDailySalesReport(date);
        
        System.out.println("\n📊 DAILY SALES REPORT - " + date);
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("Total Sales: " + String.format("EGP %.2f", report.getTotalSales()));
        System.out.println("Total Orders: " + report.getTotalOrders());
        System.out.println("Unique Customers: " + report.getTotalCustomers());
        System.out.println("Average Order Value: " + String.format("EGP %.2f", report.getAverageOrderValue()));
        
        if (!report.getItemsSold().isEmpty()) {
            System.out.println("\nTop Items Sold:");
            report.getItemsSold().entrySet().stream()
                    .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                    .limit(5)
                    .forEach(entry -> 
                        System.out.printf("  %-25s: %d units%n", entry.getKey(), entry.getValue()));
        }
        
        pressEnterToContinue();
    }
    
    private void generateWeeklySalesReport() {
        System.out.print("\n🗓️  Enter week start date (YYYY-MM-DD) or press Enter for this week: ");
        String dateStr = scanner.nextLine().trim();
        
        LocalDate startDate = dateStr.isEmpty() ? 
            LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1) : 
            LocalDate.parse(dateStr);
        
        WeeklySalesReport report = cafeteriaSystem.getReportingService()
                .generateWeeklySalesReport(startDate);
        
        System.out.println("\n📊 WEEKLY SALES REPORT");
        System.out.println("Period: " + report.getStartDate() + " to " + report.getEndDate());
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("Total Weekly Sales: " + String.format("EGP %.2f", report.getTotalWeeklySales()));
        System.out.println("Total Weekly Orders: " + report.getTotalWeeklyOrders());
        
        System.out.println("\nDaily Breakdown:");
        for (DailySalesReport daily : report.getDailyReports()) {
            System.out.printf("  %s: %s (%d orders)%n",
                daily.getDate(),
                String.format("EGP %.2f", daily.getTotalSales()),
                daily.getTotalOrders());
        }
        
        pressEnterToContinue();
    }
    
    private void generateLoyaltyPointsReport() {
        System.out.print("\n🏅 Enter start date (YYYY-MM-DD): ");
        LocalDate startDate = LocalDate.parse(scanner.nextLine().trim());
        
        System.out.print("Enter end date (YYYY-MM-DD): ");
        LocalDate endDate = LocalDate.parse(scanner.nextLine().trim());
        
        LoyaltyPointsReport report = cafeteriaSystem.getReportingService()
                .generateLoyaltyPointsReport(startDate, endDate);
        
        System.out.println("\n📊 LOYALTY POINTS REPORT");
        System.out.println("Period: " + startDate + " to " + endDate);
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("Points Earned: " + report.getTotalPointsEarned());
        System.out.println("Points Redeemed: " + report.getTotalPointsRedeemed());
        System.out.println("Total Discounts Given: " + String.format("EGP %.2f", report.getTotalDiscountsGiven()));
        
        if (!report.getTopPointEarners().isEmpty()) {
            System.out.println("\nTop Point Earners:");
            report.getTopPointEarners().entrySet().stream()
                    .limit(5)
                    .forEach(entry -> 
                        System.out.printf("  Student %s: %d points%n", entry.getKey(), entry.getValue()));
        }
        
        pressEnterToContinue();
    }
    
    private void generateTopSellingItemsReport() {
        System.out.print("\n🔥 Enter start date (YYYY-MM-DD): ");
        LocalDate startDate = LocalDate.parse(scanner.nextLine().trim());
        
        System.out.print("Enter end date (YYYY-MM-DD): ");
        LocalDate endDate = LocalDate.parse(scanner.nextLine().trim());
        
        System.out.print("Number of top items to show (default 10): ");
        String limitStr = scanner.nextLine().trim();
        int limit = limitStr.isEmpty() ? 10 : Integer.parseInt(limitStr);
        
        TopSellingItemsReport report = cafeteriaSystem.getReportingService()
                .generateTopSellingItemsReport(startDate, endDate, limit);
        
        System.out.println("\n📊 TOP SELLING ITEMS REPORT");
        System.out.println("Period: " + startDate + " to " + endDate);
        System.out.println("═══════════════════════════════════════════════════════════════");
        
        if (report.getTopItems().isEmpty()) {
            System.out.println("No sales data available for this period.");
        } else {
            for (int i = 0; i < report.getTopItems().size(); i++) {
                TopSellingItemsReport.ItemSalesData item = report.getTopItems().get(i);
                System.out.printf("%2d. %-25s: %d units, %s revenue%n",
                    i + 1,
                    item.getItemName(),
                    item.getQuantitySold(),
                    String.format("EGP %.2f", item.getTotalRevenue()));
            }
        }
        
        pressEnterToContinue();
    }
    
    private void generateStudentActivityReport() {
        System.out.print("\n👥 Enter start date (YYYY-MM-DD): ");
        LocalDate startDate = LocalDate.parse(scanner.nextLine().trim());
        
        System.out.print("Enter end date (YYYY-MM-DD): ");
        LocalDate endDate = LocalDate.parse(scanner.nextLine().trim());
        
        StudentActivityReport report = cafeteriaSystem.getReportingService()
                .generateStudentActivityReport(startDate, endDate);
        
        System.out.println("\n📊 STUDENT ACTIVITY REPORT");
        System.out.println("Period: " + startDate + " to " + endDate);
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.println("Active Students: " + report.getTotalActiveStudents());
        System.out.println("New Registrations: " + report.getNewStudentRegistrations());
        
        if (!report.getOrdersByStudent().isEmpty()) {
            System.out.println("\nMost Active Students:");
            report.getOrdersByStudent().entrySet().stream()
                    .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                    .limit(5)
                    .forEach(entry -> 
                        System.out.printf("  Student %s: %d orders%n", entry.getKey(), entry.getValue()));
        }
        
        pressEnterToContinue();
    }
    
    private void userManagement() {
        System.out.println("\n👥 USER MANAGEMENT");
        System.out.println("═══════════════════════════════════════════════════════════════");
        
        CafeteriaSystem.SystemStatistics stats = cafeteriaSystem.getSystemStatistics();
        System.out.println("Total Users: " + stats.getTotalUsers());
        System.out.println("Students: " + stats.getTotalStudents());
        System.out.println("Staff: " + stats.getTotalStaff());
        
        // Note: In a full implementation, this would include user search,
        // viewing user details, and user management functions
        System.out.println("\n🚧 Full user management features coming soon!");
        
        pressEnterToContinue();
    }
    
    private void systemNotifications() {
        System.out.println("\n🔔 SYSTEM NOTIFICATIONS");
        System.out.println("═══════════════════════════════════════════════════════════════");
        
        // Note: In a full implementation, this would show system-wide notifications,
        // maintenance alerts, etc.
        System.out.println("📢 System Status: All services operational");
        System.out.println("⚡ Performance: Normal");
        System.out.println("💾 Database: Connected");
        
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