package com.university.cafeteria.repository;

import com.university.cafeteria.model.MenuItem;
import com.university.cafeteria.model.MenuItem.MenuCategory;
import java.util.List;
import java.util.Optional;

/**
 * Interface for menu item data access operations
 * Demonstrates Dependency Inversion Principle
 */
public interface MenuRepository {
    
    /**
     * Save a menu item
     * @param menuItem Menu item to save
     * @return Saved menu item
     */
    MenuItem save(MenuItem menuItem);
    
    /**
     * Find menu item by ID
     * @param itemId Item ID to search for
     * @return Optional containing menu item if found
     */
    Optional<MenuItem> findById(String itemId);
    
    /**
     * Find all available menu items
     * @return List of available menu items
     */
    List<MenuItem> findAllAvailable();
    
    /**
     * Find all menu items (including unavailable)
     * @return List of all menu items
     */
    List<MenuItem> findAll();
    
    /**
     * Find menu items by category
     * @param category Category to filter by
     * @return List of menu items in the category
     */
    List<MenuItem> findByCategory(MenuCategory category);
    
    /**
     * Find available menu items by category
     * @param category Category to filter by
     * @return List of available menu items in the category
     */
    List<MenuItem> findAvailableByCategory(MenuCategory category);
    
    /**
     * Search menu items by name (case-insensitive)
     * @param name Name to search for
     * @return List of matching menu items
     */
    List<MenuItem> findByNameContaining(String name);
    
    /**
     * Check if item ID exists
     * @param itemId Item ID to check
     * @return true if exists, false otherwise
     */
    boolean existsById(String itemId);
    
    /**
     * Update menu item
     * @param menuItem Menu item to update
     * @return Updated menu item
     */
    MenuItem update(MenuItem menuItem);
    
    /**
     * Delete menu item by ID
     * @param itemId Item ID to delete
     * @return true if deleted, false if not found
     */
    boolean deleteById(String itemId);
    
    /**
     * Get count of menu items by category
     * @return Array where index represents category ordinal and value is count
     */
    long[] getCountByCategory();
}