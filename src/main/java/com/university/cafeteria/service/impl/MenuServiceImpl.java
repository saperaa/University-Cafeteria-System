package com.university.cafeteria.service.impl;

import com.university.cafeteria.model.MenuItem;
import com.university.cafeteria.model.MenuItem.MenuCategory;
import com.university.cafeteria.repository.MenuRepository;
import com.university.cafeteria.service.MenuService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of MenuService
 * Demonstrates Single Responsibility Principle and extensive use of Java Streams
 * Uses Dependency Injection for MenuRepository
 */
public class MenuServiceImpl implements MenuService {
    
    private final MenuRepository menuRepository;
    
    // Constants for validation
    private static final BigDecimal MIN_PRICE = new BigDecimal("0.01");
    private static final BigDecimal MAX_PRICE = new BigDecimal("1000.00");
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    
    public MenuServiceImpl(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }
    
    @Override
    public MenuItem addMenuItem(String name, String description, BigDecimal price, MenuCategory category) {
        // Validate input
        validateMenuItemData(name, price);
        validateDescription(description);
        validateCategory(category);
        
        // Generate unique item ID
        String itemId = generateItemId(name, category);
        
        // Create and save menu item
        MenuItem menuItem = new MenuItem(itemId, name.trim(), description, price, category);
        return menuRepository.save(menuItem);
    }
    
    @Override
    public MenuItem updateMenuItem(String itemId, String name, String description, BigDecimal price, MenuCategory category) {
        Optional<MenuItem> existingItemOpt = menuRepository.findById(itemId);
        if (!existingItemOpt.isPresent()) {
            throw new IllegalArgumentException("Menu item not found: " + itemId);
        }
        
        MenuItem existingItem = existingItemOpt.get();
        
        // Update fields if new values provided
        if (name != null) {
            validateItemName(name);
            existingItem.setName(name.trim());
        }
        
        if (description != null) {
            validateDescription(description);
            existingItem.setDescription(description.trim());
        }
        
        if (price != null) {
            validatePrice(price);
            existingItem.setPrice(price);
        }
        
        if (category != null) {
            existingItem.setCategory(category);
        }
        
        return menuRepository.update(existingItem);
    }
    
    @Override
    public boolean removeMenuItem(String itemId) {
        if (itemId == null || itemId.trim().isEmpty()) {
            return false;
        }
        
        return menuRepository.deleteById(itemId.trim());
    }
    
    @Override
    public boolean setItemAvailability(String itemId, boolean available) {
        Optional<MenuItem> itemOpt = menuRepository.findById(itemId);
        if (!itemOpt.isPresent()) {
            return false;
        }
        
        MenuItem item = itemOpt.get();
        item.setAvailable(available);
        menuRepository.update(item);
        return true;
    }
    
    @Override
    public List<MenuItem> getAvailableMenuItems() {
        return menuRepository.findAllAvailable();
    }
    
    @Override
    public List<MenuItem> getAllMenuItems() {
        return menuRepository.findAll();
    }
    
    @Override
    public List<MenuItem> getMenuItemsByCategory(MenuCategory category, boolean availableOnly) {
        if (category == null) {
            return availableOnly ? getAvailableMenuItems() : getAllMenuItems();
        }
        
        return availableOnly ? 
            menuRepository.findAvailableByCategory(category) : 
            menuRepository.findByCategory(category);
    }
    
    @Override
    public List<MenuItem> searchMenuItems(String searchTerm, boolean availableOnly) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return availableOnly ? getAvailableMenuItems() : getAllMenuItems();
        }
        
        List<MenuItem> searchResults = menuRepository.findByNameContaining(searchTerm.trim());
        
        if (availableOnly) {
            return searchResults.stream()
                    .filter(MenuItem::isAvailable)
                    .collect(Collectors.toList());
        }
        
        return searchResults;
    }
    
    @Override
    public Optional<MenuItem> getMenuItemById(String itemId) {
        if (itemId == null) {
            return Optional.empty();
        }
        return menuRepository.findById(itemId.trim());
    }
    
    @Override
    public List<MenuItem> getAffordableMenuItems(BigDecimal budget, boolean availableOnly) {
        if (budget == null || budget.compareTo(BigDecimal.ZERO) <= 0) {
            return List.of(); // Return empty list for invalid budget
        }
        
        List<MenuItem> items = availableOnly ? getAvailableMenuItems() : getAllMenuItems();
        
        return items.stream()
                .filter(item -> item.isAffordableFor(budget))
                .collect(Collectors.toList());
    }
    
    @Override
    public long[] getMenuStatistics() {
        return menuRepository.getCountByCategory();
    }
    
    @Override
    public boolean validateMenuItemData(String name, BigDecimal price) {
        try {
            validateItemName(name);
            validatePrice(price);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Additional business logic methods
     */
    
    /**
     * Get featured items (most expensive items from each category)
     */
    public List<MenuItem> getFeaturedItems() {
        return getAvailableMenuItems().stream()
                .collect(Collectors.groupingBy(
                    MenuItem::getCategory,
                    Collectors.maxBy((item1, item2) -> item1.getPrice().compareTo(item2.getPrice()))
                ))
                .values().stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
    
    /**
     * Get budget-friendly items (items under a specific price)
     */
    public List<MenuItem> getBudgetFriendlyItems(BigDecimal maxPrice) {
        return getAvailableMenuItems().stream()
                .filter(item -> item.getPrice().compareTo(maxPrice) <= 0)
                .sorted((item1, item2) -> item1.getPrice().compareTo(item2.getPrice()))
                .collect(Collectors.toList());
    }
    

    
    /**
     * Validation helper methods
     */
    
    private void validateItemName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be null or empty");
        }
        
        if (name.trim().length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Item name cannot exceed " + MAX_NAME_LENGTH + " characters");
        }
    }
    
    private void validateDescription(String description) {
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("Description cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters");
        }
    }
    
    private void validatePrice(BigDecimal price) {
        if (price == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        
        if (price.compareTo(MIN_PRICE) < 0) {
            throw new IllegalArgumentException("Price must be at least " + MIN_PRICE);
        }
        
        if (price.compareTo(MAX_PRICE) > 0) {
            throw new IllegalArgumentException("Price cannot exceed " + MAX_PRICE);
        }
    }
    
    private void validateCategory(MenuCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
    }
    
    /**
     * Generate unique item ID
     */
    private String generateItemId(String name, MenuCategory category) {
        // Create a base ID from category and name
        String cleanName = name.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        int nameLength = Math.min(5, cleanName.length());
        
        // Ensure we don't go out of bounds
        if (nameLength <= 0) {
            nameLength = 1; // Fallback to at least 1 character
        }
        
        String baseId = category.name().substring(0, 3) + "_" + 
                       cleanName.substring(0, nameLength);
        
        // Add unique suffix
        String itemId = baseId + "_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        
        // Ensure uniqueness (in case of collision)
        int counter = 1;
        String finalId = itemId;
        while (menuRepository.existsById(finalId)) {
            finalId = itemId + "_" + counter++;
        }
        
        return finalId;
    }
}