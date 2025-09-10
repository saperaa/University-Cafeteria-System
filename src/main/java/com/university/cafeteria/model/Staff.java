package com.university.cafeteria.model;

/**
 * Staff class representing cafeteria staff members
 * Extends User and includes staff-specific functionality
 */
public class Staff extends User {
    private String employeeId;
    private StaffRole role;
    
    public enum StaffRole {
        ADMIN("Administrator - Full system access"),
        CASHIER("Cashier - Order processing"),
        KITCHEN("Kitchen Staff - Order preparation"),
        MANAGER("Manager - Reports and oversight");
        
        private final String description;
        
        StaffRole(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public Staff(String userId, String name, String password, String employeeId, StaffRole role) {
        super(userId, name, password);
        this.employeeId = employeeId;
        this.role = role;
    }
    
    @Override
    public String getUserType() {
        return "STAFF";
    }
    
    // Staff-specific getters
    public String getEmployeeId() { return employeeId; }
    public StaffRole getRole() { return role; }
    
    // Permission checking methods
    public boolean canManageMenu() {
        return role == StaffRole.ADMIN || role == StaffRole.MANAGER;
    }
    
    public boolean canProcessOrders() {
        return role == StaffRole.ADMIN || role == StaffRole.CASHIER || role == StaffRole.KITCHEN;
    }
    
    public boolean canViewReports() {
        return role == StaffRole.ADMIN || role == StaffRole.MANAGER;
    }
    
    @Override
    public String toString() {
        return String.format("Staff{userId='%s', name='%s', employeeId='%s', role='%s'}", 
            userId, name, employeeId, role);
    }
}