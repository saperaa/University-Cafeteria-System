package com.university.cafeteria.service;

import com.university.cafeteria.model.User;
import com.university.cafeteria.model.Student;
import com.university.cafeteria.model.Staff;
import java.util.Optional;

/**
 * Interface for authentication and user management services
 * Demonstrates Single Responsibility Principle - focused on authentication only
 */
public interface AuthenticationService {
    
    /**
     * Register a new student
     * @param userId Unique user ID
     * @param name Student name
     * @param password Password
     * @param studentId University student ID
     * @return Registered student
     * @throws IllegalArgumentException if validation fails
     */
    Student registerStudent(String userId, String name, String password, String studentId);
    
    /**
     * Register a new staff member
     * @param userId Unique user ID
     * @param name Staff name
     * @param password Password
     * @param employeeId Employee ID
     * @param role Staff role
     * @return Registered staff member
     * @throws IllegalArgumentException if validation fails
     */
    Staff registerStaff(String userId, String name, String password, String employeeId, Staff.StaffRole role);
    
    /**
     * Authenticate user login
     * @param userId User ID
     * @param password Password
     * @return Optional containing authenticated user if successful
     */
    Optional<User> login(String userId, String password);
    
    /**
     * Validate user credentials
     * @param userId User ID
     * @param password Password
     * @return true if credentials are valid
     */
    boolean validateCredentials(String userId, String password);
    
    /**
     * Check if user ID is available
     * @param userId User ID to check
     * @return true if available, false if taken
     */
    boolean isUserIdAvailable(String userId);
    
    /**
     * Check if student ID is available
     * @param studentId Student ID to check
     * @return true if available, false if taken
     */
    boolean isStudentIdAvailable(String studentId);
    
    /**
     * Check if employee ID is available
     * @param employeeId Employee ID to check
     * @return true if available, false if taken
     */
    boolean isEmployeeIdAvailable(String employeeId);
    
    /**
     * Find user by ID
     * @param userId User ID to find
     * @return Optional containing user if found
     */
    Optional<User> findUserById(String userId);
    
    /**
     * Find student by student ID
     * @param studentId Student ID to find
     * @return Optional containing student if found
     */
    Optional<Student> findStudentByStudentId(String studentId);
    
    /**
     * Update user password
     * @param userId User ID
     * @param oldPassword Current password
     * @param newPassword New password
     * @return true if password updated successfully
     */
    boolean updatePassword(String userId, String oldPassword, String newPassword);
    
    /**
     * Get total number of users in the system
     * @return Total user count
     */
    long getTotalUserCount();
    
    /**
     * Get total number of students in the system
     * @return Total student count
     */
    long getStudentCount();
    
    /**
     * Get total number of staff members in the system
     * @return Total staff count
     */
    long getStaffCount();
}