package com.university.cafeteria.service;

import com.university.cafeteria.model.MenuItem;
import com.university.cafeteria.model.MenuItem.MenuCategory;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Interface for menu management services
 * Demonstrates Single Responsibility Principle - focused on menu operations only
 */
public interface MenuService {
    
    /**
     * Add a new menu item (admin only)
     * @param name Item name
     * @param description Item description
     * @param price Item price
     * @param category Item category
     * @return Created menu item
     * @throws IllegalArgumentException if validation fails
     */
    MenuItem addMenuItem(String name, String description, BigDecimal price, MenuCategory category);
    
    /**
     * Update an existing menu item (admin only)
     * @param itemId Item ID to update
     * @param name New name (null to keep current)
     * @param description New description (null to keep current)
     * @param price New price (null to keep current)
     * @param category New category (null to keep current)
     * @return Updated menu item
     * @throws IllegalArgumentException if item not found or validation fails
     */
    MenuItem updateMenuItem(String itemId, String name, String description, BigDecimal price, MenuCategory category);
    
    /**
     * Remove a menu item (admin only)
     * @param itemId Item ID to remove
     * @return true if removed, false if not found
     */
    boolean removeMenuItem(String itemId);
    
    /**
     * Set menu item availability
     * @param itemId Item ID
     * @param available Availability status
     * @return true if updated, false if item not found
     */
    boolean setItemAvailability(String itemId, boolean available);
    
    /**
     * Get all available menu items for customers
     * @return List of available menu items
     */
    List<MenuItem> getAvailableMenuItems();
    
    /**
     * Get all menu items (including unavailable) for admin
     * @return List of all menu items
     */
    List<MenuItem> getAllMenuItems();
    
    /**
     * Get menu items by category
     * @param category Category to filter by
     * @param availableOnly Whether to include only available items
     * @return List of menu items in the category
     */
    List<MenuItem> getMenuItemsByCategory(MenuCategory category, boolean availableOnly);
    
    /**
     * Search menu items by name
     * @param searchTerm Search term (case-insensitive)
     * @param availableOnly Whether to include only available items
     * @return List of matching menu items
     */
    List<MenuItem> searchMenuItems(String searchTerm, boolean availableOnly);
    
    /**
     * Get menu item by ID
     * @param itemId Item ID
     * @return Optional containing menu item if found
     */
    Optional<MenuItem> getMenuItemById(String itemId);
    
    /**
     * Get menu items affordable within a budget
     * @param budget Maximum price to filter by
     * @param availableOnly Whether to include only available items
     * @return List of affordable menu items
     */
    List<MenuItem> getAffordableMenuItems(BigDecimal budget, boolean availableOnly);
    
    /**
     * Get menu statistics
     * @return Array of item counts by category
     */
    long[] getMenuStatistics();
    
    /**
     * Validate menu item data
     * @param name Item name
     * @param price Item price
     * @return true if valid, false otherwise
     */
    boolean validateMenuItemData(String name, BigDecimal price);
}