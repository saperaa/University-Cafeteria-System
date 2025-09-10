package com.university.cafeteria.repository.impl;

import com.university.cafeteria.model.MenuItem;
import com.university.cafeteria.model.MenuItem.MenuCategory;
import com.university.cafeteria.repository.MenuRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of MenuRepository
 * Demonstrates repository pattern with thread-safe operations and extensive use of Java Streams
 */
public class InMemoryMenuRepository implements MenuRepository {
    
    // Thread-safe concurrent map for storing menu items
    private final Map<String, MenuItem> menuItemsById = new ConcurrentHashMap<>();
    
    @Override
    public MenuItem save(MenuItem menuItem) {
        if (menuItem == null) {
            throw new IllegalArgumentException("Menu item cannot be null");
        }
        
        menuItemsById.put(menuItem.getItemId(), menuItem);
        return menuItem;
    }
    
    @Override
    public Optional<MenuItem> findById(String itemId) {
        if (itemId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(menuItemsById.get(itemId));
    }
    
    @Override
    public List<MenuItem> findAllAvailable() {
        // Using streams to filter available items and sort by category then name
        return menuItemsById.values().stream()
                .filter(MenuItem::isAvailable)
                .sorted(Comparator
                    .comparing((MenuItem item) -> item.getCategory().ordinal())
                    .thenComparing(MenuItem::getName))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<MenuItem> findAll() {
        // Sort all items by category then name
        return menuItemsById.values().stream()
                .sorted(Comparator
                    .comparing((MenuItem item) -> item.getCategory().ordinal())
                    .thenComparing(MenuItem::getName))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<MenuItem> findByCategory(MenuCategory category) {
        if (category == null) {
            return new ArrayList<>();
        }
        
        return menuItemsById.values().stream()
                .filter(item -> item.getCategory() == category)
                .sorted(Comparator.comparing(MenuItem::getName))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<MenuItem> findAvailableByCategory(MenuCategory category) {
        if (category == null) {
            return new ArrayList<>();
        }
        
        return menuItemsById.values().stream()
                .filter(item -> item.getCategory() == category && item.isAvailable())
                .sorted(Comparator.comparing(MenuItem::getName))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<MenuItem> findByNameContaining(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String searchTerm = name.trim().toLowerCase();
        return menuItemsById.values().stream()
                .filter(item -> item.getName().toLowerCase().contains(searchTerm) ||
                               (item.getDescription() != null && 
                                item.getDescription().toLowerCase().contains(searchTerm)))
                .sorted(Comparator.comparing(MenuItem::getName))
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsById(String itemId) {
        return itemId != null && menuItemsById.containsKey(itemId);
    }
    
    @Override
    public MenuItem update(MenuItem menuItem) {
        if (menuItem == null) {
            throw new IllegalArgumentException("Menu item cannot be null");
        }
        
        if (!menuItemsById.containsKey(menuItem.getItemId())) {
            throw new IllegalArgumentException("Menu item not found: " + menuItem.getItemId());
        }
        
        return save(menuItem); // save method handles both create and update
    }
    
    @Override
    public boolean deleteById(String itemId) {
        if (itemId == null) {
            return false;
        }
        
        return menuItemsById.remove(itemId) != null;
    }
    
    @Override
    public long[] getCountByCategory() {
        MenuCategory[] categories = MenuCategory.values();
        long[] counts = new long[categories.length];
        
        // Use streams to count items by category
        Map<MenuCategory, Long> categoryCount = menuItemsById.values().stream()
                .collect(Collectors.groupingBy(
                    MenuItem::getCategory,
                    Collectors.counting()
                ));
        
        // Fill the array with counts
        for (int i = 0; i < categories.length; i++) {
            counts[i] = categoryCount.getOrDefault(categories[i], 0L);
        }
        
        return counts;
    }
    
    /**
     * Additional utility methods for enhanced functionality
     */
    
    public int getMenuItemCount() {
        return menuItemsById.size();
    }
    
    public int getAvailableItemCount() {
        return (int) menuItemsById.values().stream()
                .filter(MenuItem::isAvailable)
                .count();
    }
    
    public void clear() {
        menuItemsById.clear();
    }
    
    /**
     * Find items within a price range using streams
     */
    public List<MenuItem> findByPriceRange(double minPrice, double maxPrice, boolean availableOnly) {
        return menuItemsById.values().stream()
                .filter(item -> !availableOnly || item.isAvailable())
                .filter(item -> {
                    double price = item.getPrice().doubleValue();
                    return price >= minPrice && price <= maxPrice;
                })
                .sorted(Comparator.comparing(MenuItem::getPrice))
                .collect(Collectors.toList());
    }
    
    /**
     * Get most expensive items by category
     */
    public Map<MenuCategory, Optional<MenuItem>> getMostExpensiveByCategory() {
        return menuItemsById.values().stream()
                .filter(MenuItem::isAvailable)
                .collect(Collectors.groupingBy(
                    MenuItem::getCategory,
                    Collectors.maxBy(Comparator.comparing(MenuItem::getPrice))
                ));
    }
    
    /**
     * Get least expensive items by category
     */
    public Map<MenuCategory, Optional<MenuItem>> getLeastExpensiveByCategory() {
        return menuItemsById.values().stream()
                .filter(MenuItem::isAvailable)
                .collect(Collectors.groupingBy(
                    MenuItem::getCategory,
                    Collectors.minBy(Comparator.comparing(MenuItem::getPrice))
                ));
    }
    
    /**
     * Get average price by category
     */
    public Map<MenuCategory, Double> getAveragePriceByCategory() {
        return menuItemsById.values().stream()
                .filter(MenuItem::isAvailable)
                .collect(Collectors.groupingBy(
                    MenuItem::getCategory,
                    Collectors.averagingDouble(item -> item.getPrice().doubleValue())
                ));
    }
    
    /**
     * Find recently added items (within last N days)
     */
    public List<MenuItem> findRecentlyAdded(int days) {
        java.time.LocalDateTime cutoff = java.time.LocalDateTime.now().minusDays(days);
        
        return menuItemsById.values().stream()
                .filter(item -> item.getCreatedAt().isAfter(cutoff))
                .sorted(Comparator.comparing(MenuItem::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * Find recently updated items (within last N days)
     */
    public List<MenuItem> findRecentlyUpdated(int days) {
        java.time.LocalDateTime cutoff = java.time.LocalDateTime.now().minusDays(days);
        
        return menuItemsById.values().stream()
                .filter(item -> item.getUpdatedAt().isAfter(cutoff))
                .filter(item -> !item.getCreatedAt().equals(item.getUpdatedAt())) // Exclude newly created items
                .sorted(Comparator.comparing(MenuItem::getUpdatedAt).reversed())
                .collect(Collectors.toList());
    }
}