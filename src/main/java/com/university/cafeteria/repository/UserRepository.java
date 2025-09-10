package com.university.cafeteria.repository;

import com.university.cafeteria.model.User;
import com.university.cafeteria.model.Student;
import com.university.cafeteria.model.Staff;
import java.util.List;
import java.util.Optional;

/**
 * Interface for user data access operations
 * Follows Dependency Inversion Principle (DIP) - high-level modules depend on abstractions
 */
public interface UserRepository {
    
    /**
     * Save a user to the repository
     * @param user User to save
     * @return Saved user
     */
    User save(User user);
    
    /**
     * Find user by ID
     * @param userId User ID to search for
     * @return Optional containing user if found
     */
    Optional<User> findById(String userId);
    
    /**
     * Find student by student ID
     * @param studentId Student ID to search for
     * @return Optional containing student if found
     */
    Optional<Student> findStudentByStudentId(String studentId);
    
    /**
     * Find staff by employee ID
     * @param employeeId Employee ID to search for
     * @return Optional containing staff if found
     */
    Optional<Staff> findStaffByEmployeeId(String employeeId);
    
    /**
     * Check if user ID exists
     * @param userId User ID to check
     * @return true if exists, false otherwise
     */
    boolean existsById(String userId);
    
    /**
     * Check if student ID exists
     * @param studentId Student ID to check
     * @return true if exists, false otherwise
     */
    boolean existsByStudentId(String studentId);
    
    /**
     * Check if employee ID exists
     * @param employeeId Employee ID to check
     * @return true if exists, false otherwise
     */
    boolean existsByEmployeeId(String employeeId);
    
    /**
     * Get all students
     * @return List of all students
     */
    List<Student> findAllStudents();
    
    /**
     * Get all staff members
     * @return List of all staff
     */
    List<Staff> findAllStaff();
    
    /**
     * Update user information
     * @param user User to update
     * @return Updated user
     */
    User update(User user);
    
    /**
     * Delete user by ID
     * @param userId User ID to delete
     * @return true if deleted, false if not found
     */
    boolean deleteById(String userId);
}