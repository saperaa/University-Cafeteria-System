package com.university.cafeteria;

import com.university.cafeteria.service.CafeteriaSystem;
import com.university.cafeteria.ui.ConsoleInterface;
import com.university.cafeteria.gui.CafeteriaGUI;


public class Main {
    public static void main(String[] args) {
        System.out.println("=== University Cafeteria Order & Loyalty System ===");
        System.out.println("Initializing system...\n");
        
        boolean useGUI = true;
        if (args.length > 0 && "console".equalsIgnoreCase(args[0])) {
            useGUI = false;
        }
        
        if (useGUI) {
            System.out.println("Starting GUI mode...");
            CafeteriaGUI.main(args);
        } else {
            CafeteriaSystem cafeteriaSystem = new CafeteriaSystem();
            
            int drinkCount = (int) cafeteriaSystem.getMenuService().getAllMenuItems().stream()
                .filter(item -> item.getCategory() == com.university.cafeteria.model.MenuItem.MenuCategory.DRINK)
                .count();
            
            System.out.println("🥤 Found " + drinkCount + " drink items in menu");
            if (drinkCount == 0) {
                System.out.println("⚠️  No drinks found! Refreshing menu from database...");
                cafeteriaSystem.refreshMenuFromDatabase();
                
                drinkCount = (int) cafeteriaSystem.getMenuService().getAllMenuItems().stream()
                    .filter(item -> item.getCategory() == com.university.cafeteria.model.MenuItem.MenuCategory.DRINK)
                    .count();
                System.out.println("🥤 After refresh: " + drinkCount + " drink items available");
            }
            System.out.println("Starting console interface...");
            ConsoleInterface consoleInterface = new ConsoleInterface(cafeteriaSystem);
            consoleInterface.start();
        }
    }
}