package com.university.cafeteria.ui;

import com.university.cafeteria.model.User;
import com.university.cafeteria.model.Student;
import com.university.cafeteria.model.Staff;
import com.university.cafeteria.service.CafeteriaSystem;

import java.util.Optional;
import java.util.Scanner;

/**
 * Main console interface for the cafeteria system
 * Demonstrates user interaction patterns and session management
 */
public class ConsoleInterface {
    
    private final CafeteriaSystem cafeteriaSystem;
    private final Scanner scanner;
    private User currentUser;
    private boolean running;
    
    // UI Components
    private StudentInterface studentInterface;
    private StaffInterface staffInterface;
    
    public ConsoleInterface(CafeteriaSystem cafeteriaSystem) {
        this.cafeteriaSystem = cafeteriaSystem;
        this.scanner = new Scanner(System.in);
        this.running = true;
        
        // Initialize UI components
        this.studentInterface = new StudentInterface(cafeteriaSystem, scanner);
        this.staffInterface = new StaffInterface(cafeteriaSystem, scanner);
    }
    
    /**
     * Start the console interface
     */
    public void start() {
        printWelcomeMessage();
        
        while (running) {
            try {
                if (currentUser == null) {
                    showMainMenu();
                } else {
                    showUserDashboard();
                }
            } catch (Exception e) {
                System.err.println("An error occurred: " + e.getMessage());
                System.out.println("Press Enter to continue...");
                scanner.nextLine();
            }
        }
        
        scanner.close();
        cafeteriaSystem.shutdown();
    }
    
    private void printWelcomeMessage() {
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║              🍽️  UNIVERSITY CAFETERIA SYSTEM  🍽️                 ║");
        System.out.println("║                                                                  ║");
        System.out.println("║              Welcome to our Digital Ordering System!            ║");
        System.out.println("║                                                                  ║");
        System.out.println("║  Features:                                                       ║");
        System.out.println("║  • Digital Menu & Ordering                                      ║");
        System.out.println("║  • Loyalty Points Program                                       ║");
        System.out.println("║  • Order Tracking                                               ║");
        System.out.println("║  • Staff Management Tools                                       ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println();
    }
    
    private void showMainMenu() {
        System.out.println("═══════════════════════════════════════");
        System.out.println("           🏠 MAIN MENU");
        System.out.println("═══════════════════════════════════════");
        System.out.println("1. 🎓 Student Login");
        System.out.println("2. 📝 Student Registration");
        System.out.println("3. 👨‍💼 Staff Login");
        System.out.println("4. 📊 View System Statistics");
        System.out.println("5. 🔍 Debug: View All Users");
        System.out.println("6. ❓ Help");
        System.out.println("7. 🚪 Exit");
        System.out.println("═══════════════════════════════════════");
        System.out.print("Please select an option (1-7): ");
        
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            handleMainMenuChoice(choice);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid input. Please enter a number between 1 and 6.");
            pressEnterToContinue();
        }
    }
    
    private void handleMainMenuChoice(int choice) {
        switch (choice) {
            case 1 -> studentLogin();
            case 2 -> studentRegistration();
            case 3 -> staffLogin();
            case 4 -> showSystemStatistics();
            case 5 -> showDebugUsers();
            case 6 -> showHelp();
            case 7 -> exitSystem();
            default -> {
                System.out.println("❌ Invalid option. Please select a number between 1 and 7.");
                pressEnterToContinue();
            }
        }
    }
    
    private void studentLogin() {
        System.out.println("\n🎓 STUDENT LOGIN");
        System.out.println("────────────────────");
        System.out.print("Enter your User ID: ");
        String userId = scanner.nextLine().trim();
        
        if (userId.isEmpty()) {
            System.out.println("❌ User ID cannot be empty.");
            pressEnterToContinue();
            return;
        }
        
        System.out.print("Enter your Password: ");
        String password = scanner.nextLine().trim();
        
        Optional<User> userOpt = cafeteriaSystem.getAuthenticationService().login(userId, password);
        
        if (userOpt.isPresent() && userOpt.get() instanceof Student) {
            currentUser = userOpt.get();
            System.out.println("✅ Login successful! Welcome, " + currentUser.getName() + "!");
            pressEnterToContinue();
        } else {
            System.out.println("❌ Invalid credentials or not a student account.");
            pressEnterToContinue();
        }
    }
    
    private void studentRegistration() {
        System.out.println("\n📝 STUDENT REGISTRATION");
        System.out.println("────────────────────────");
        
        try {
            System.out.print("Enter your Full Name: ");
            String name = scanner.nextLine().trim();
            
            if (name.isEmpty()) {
                System.out.println("❌ Name cannot be empty.");
                pressEnterToContinue();
                return;
            }
            
            System.out.print("Enter desired User ID (3-20 characters): ");
            String userId = scanner.nextLine().trim();
            
            System.out.print("Enter your University Student ID: ");
            String studentId = scanner.nextLine().trim();
            
            System.out.print("Enter your Password (minimum 6 characters): ");
            String password = scanner.nextLine().trim();
            
            Student newStudent = cafeteriaSystem.getAuthenticationService()
                    .registerStudent(userId, name, password, studentId);
            
            System.out.println("✅ Registration successful!");
            System.out.println("📧 Welcome message sent to your account.");
            System.out.println("🎁 You've received welcome bonus loyalty points!");
            System.out.println("\n👤 Your Account Details:");
            System.out.println("   User ID: " + newStudent.getUserId());
            System.out.println("   Name: " + newStudent.getName());
            System.out.println("   Student ID: " + newStudent.getStudentId());
            System.out.println("   Loyalty Points: " + newStudent.getLoyaltyPoints());
            
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Registration failed: " + e.getMessage());
        }
        
        pressEnterToContinue();
    }
    
    private void staffLogin() {
        System.out.println("\n👨‍💼 STAFF LOGIN");
        System.out.println("──────────────────");
        System.out.print("Enter your User ID: ");
        String userId = scanner.nextLine().trim();
        
        if (userId.isEmpty()) {
            System.out.println("❌ User ID cannot be empty.");
            pressEnterToContinue();
            return;
        }
        
        System.out.print("Enter your Password: ");
        String password = scanner.nextLine().trim();
        
        Optional<User> userOpt = cafeteriaSystem.getAuthenticationService().login(userId, password);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("🔍 Debug: Found user: " + user.getUserId() + " (" + user.getClass().getSimpleName() + ")");
            
            if (user instanceof Staff) {
                currentUser = user;
                Staff staff = (Staff) currentUser;
                System.out.println("✅ Login successful!");
                System.out.println("👋 Welcome, " + staff.getName() + " (" + staff.getRole().name() + ")!");
                pressEnterToContinue();
            } else {
                System.out.println("❌ User found but not a staff account. User type: " + user.getUserType());
                pressEnterToContinue();
            }
        } else {
            System.out.println("❌ Invalid credentials - user not found or password incorrect.");
            pressEnterToContinue();
        }
    }
    
    private void showSystemStatistics() {
        System.out.println("\n📊 SYSTEM STATISTICS");
        System.out.println("─────────────────────");
        
        CafeteriaSystem.SystemStatistics stats = cafeteriaSystem.getSystemStatistics();
        
        System.out.println("👥 Users:");
        System.out.println("   Total Users: " + stats.getTotalUsers());
        System.out.println("   Students: " + stats.getTotalStudents());
        System.out.println("   Staff: " + stats.getTotalStaff());
        System.out.println();
        System.out.println("🍽️ Menu:");
        System.out.println("   Total Items: " + stats.getTotalMenuItems());
        System.out.println("   Available Items: " + stats.getAvailableMenuItems());
        System.out.println();
        System.out.println("📦 Orders:");
        System.out.println("   Total Orders: " + stats.getTotalOrders());
        
        pressEnterToContinue();
    }
    
    private void showDebugUsers() {
        System.out.println("\n🔍 DEBUG: ALL REGISTERED USERS");
        System.out.println("─────────────────────────────────");
        
        try {
            // Get user repository through reflection or add a debug method
            System.out.println("Staff accounts that should exist:");
            System.out.println("  admin / admin123");
            System.out.println("  manager1 / manager123");
            System.out.println("  cashier1 / cashier123");
            System.out.println("  kitchen1 / kitchen123");
            System.out.println();
            
            // Test each staff login
            System.out.println("Testing staff logins:");
            testLogin("admin", "admin123");
            testLogin("manager1", "manager123");
            testLogin("cashier1", "cashier123");
            testLogin("kitchen1", "kitchen123");
            
            // Additionally show database counts to help debug mismatches
            String url = System.getProperty("cafeteria.db.url");
            if (url != null && !url.isEmpty()) {
                System.out.println();
                System.out.println("DB connection: " + url);
                try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                        url,
                        System.getProperty("cafeteria.db.user"),
                        System.getProperty("cafeteria.db.password"))) {
                    long users = count(conn, "SELECT COUNT(*) FROM users");
                    long students = count(conn, "SELECT COUNT(*) FROM students");
                    long staff = count(conn, "SELECT COUNT(*) FROM staff");
                    long menu = count(conn, "SELECT COUNT(*) FROM menu_items");
                    long orders = count(conn, "SELECT COUNT(*) FROM orders");
                    System.out.println("DB counts -> users:" + users + ", students:" + students + ", staff:" + staff + ", menu:" + menu + ", orders:" + orders);
                } catch (Exception ex) {
                    System.out.println("DB check failed: " + ex.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error during debug: " + e.getMessage());
            e.printStackTrace();
        }
        
        pressEnterToContinue();
    }

    private long count(java.sql.Connection conn, String sql) throws java.sql.SQLException {
        try (java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }
    
    private void testLogin(String userId, String password) {
        try {
            Optional<User> userOpt = cafeteriaSystem.getAuthenticationService().login(userId, password);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                System.out.println("  ✅ " + userId + " -> " + user.getClass().getSimpleName() + " (" + user.getUserType() + ")");
            } else {
                System.out.println("  ❌ " + userId + " -> Login failed");
            }
        } catch (Exception e) {
            System.out.println("  ❌ " + userId + " -> Error: " + e.getMessage());
        }
    }
    
    private void showHelp() {
        System.out.println("\n❓ HELP & INFORMATION");
        System.out.println("──────────────────────");
        System.out.println("📖 How to use the system:");
        System.out.println();
        System.out.println("👨‍🎓 STUDENTS:");
        System.out.println("  • Register with your university student ID");
        System.out.println("  • Browse menu and place orders");
        System.out.println("  • Earn loyalty points (1 point per 10 EGP spent)");
        System.out.println("  • Redeem points for discounts");
        System.out.println("  • Track your order status");
        System.out.println();
        System.out.println("👨‍💼 STAFF:");
        System.out.println("  • Manage menu items (Admin/Manager)");
        System.out.println("  • Process orders (All staff)");
        System.out.println("  • View reports (Admin/Manager)");
        System.out.println("  • Update order status");
        System.out.println();
        System.out.println("🏅 LOYALTY PROGRAM:");
        System.out.println("  • Earn 1 point for every 10 EGP spent");
        System.out.println("  • Redeem points for discounts");
        System.out.println("  • Special offers for loyal customers");
        System.out.println();
        System.out.println("📧 DEMO ACCOUNTS:");
        System.out.println("  Staff: admin/admin123, manager1/manager123");
        System.out.println("  Students: student1/student123, student2/student123");
        
        pressEnterToContinue();
    }
    
    private void exitSystem() {
        System.out.println("\n👋 Thank you for using the University Cafeteria System!");
        System.out.println("Have a great day! 🌟");
        running = false;
    }
    
    private void showUserDashboard() {
        if (currentUser instanceof Student) {
            studentInterface.showStudentDashboard((Student) currentUser);
            if (studentInterface.shouldLogout()) {
                logout();
            }
        } else if (currentUser instanceof Staff) {
            staffInterface.showStaffDashboard((Staff) currentUser);
            if (staffInterface.shouldLogout()) {
                logout();
            }
        }
    }
    
    private void logout() {
        System.out.println("👋 Logged out successfully. See you next time!");
        currentUser = null;
        pressEnterToContinue();
    }
    
    private void pressEnterToContinue() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Get current logged-in user
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Check if system is running
     */
    public boolean isRunning() {
        return running;
    }
}