package com.university.cafeteria.repository.impl;

import com.university.cafeteria.model.User;
import com.university.cafeteria.model.Student;
import com.university.cafeteria.model.Staff;
import com.university.cafeteria.repository.UserRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of UserRepository
 * Demonstrates repository pattern and thread-safe operations using ConcurrentHashMap
 * Uses Java Streams for data filtering and processing
 */
public class InMemoryUserRepository implements UserRepository {
    
    // Thread-safe concurrent maps for storing users
    private final Map<String, User> usersById = new ConcurrentHashMap<>();
    private final Map<String, Student> studentsByStudentId = new ConcurrentHashMap<>();
    private final Map<String, Staff> staffByEmployeeId = new ConcurrentHashMap<>();
    
    @Override
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        usersById.put(user.getUserId(), user);
        
        // Add to specific type maps
        if (user instanceof Student) {
            Student student = (Student) user;
            studentsByStudentId.put(student.getStudentId(), student);
        } else if (user instanceof Staff) {
            Staff staff = (Staff) user;
            staffByEmployeeId.put(staff.getEmployeeId(), staff);
        }
        
        return user;
    }
    
    @Override
    public Optional<User> findById(String userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(usersById.get(userId));
    }
    
    @Override
    public Optional<Student> findStudentByStudentId(String studentId) {
        if (studentId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(studentsByStudentId.get(studentId));
    }
    
    @Override
    public Optional<Staff> findStaffByEmployeeId(String employeeId) {
        if (employeeId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(staffByEmployeeId.get(employeeId));
    }
    
    @Override
    public boolean existsById(String userId) {
        return userId != null && usersById.containsKey(userId);
    }
    
    @Override
    public boolean existsByStudentId(String studentId) {
        return studentId != null && studentsByStudentId.containsKey(studentId);
    }
    
    @Override
    public boolean existsByEmployeeId(String employeeId) {
        return employeeId != null && staffByEmployeeId.containsKey(employeeId);
    }
    
    @Override
    public List<Student> findAllStudents() {
        // Using streams to filter and collect students
        return usersById.values().stream()
                .filter(user -> user instanceof Student)
                .map(user -> (Student) user)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Staff> findAllStaff() {
        // Using streams to filter and collect staff
        return usersById.values().stream()
                .filter(user -> user instanceof Staff)
                .map(user -> (Staff) user)
                .collect(Collectors.toList());
    }
    
    @Override
    public User update(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        
        if (!usersById.containsKey(user.getUserId())) {
            throw new IllegalArgumentException("User not found: " + user.getUserId());
        }
        
        return save(user); // save method handles both create and update
    }
    
    @Override
    public boolean deleteById(String userId) {
        if (userId == null) {
            return false;
        }
        
        User user = usersById.remove(userId);
        if (user == null) {
            return false;
        }
        
        // Remove from specific type maps
        if (user instanceof Student) {
            Student student = (Student) user;
            studentsByStudentId.remove(student.getStudentId());
        } else if (user instanceof Staff) {
            Staff staff = (Staff) user;
            staffByEmployeeId.remove(staff.getEmployeeId());
        }
        
        return true;
    }
    
    /**
     * Additional utility methods for testing and debugging
     */
    
    public int getUserCount() {
        return usersById.size();
    }
    
    public int getStudentCount() {
        return studentsByStudentId.size();
    }
    
    public int getStaffCount() {
        return staffByEmployeeId.size();
    }
    
    public void clear() {
        usersById.clear();
        studentsByStudentId.clear();
        staffByEmployeeId.clear();
    }
    
    /**
     * Get user statistics using streams
     */
    public Map<String, Long> getUserStatistics() {
        return usersById.values().stream()
                .collect(Collectors.groupingBy(
                    User::getUserType,
                    Collectors.counting()
                ));
    }
    
    /**
     * Find students by loyalty points range (demonstration of advanced querying)
     */
    public List<Student> findStudentsByLoyaltyPointsRange(int minPoints, int maxPoints) {
        return findAllStudents().stream()
                .filter(student -> {
                    int points = student.getLoyaltyPoints();
                    return points >= minPoints && points <= maxPoints;
                })
                .sorted((s1, s2) -> Integer.compare(s2.getLoyaltyPoints(), s1.getLoyaltyPoints()))
                .collect(Collectors.toList());
    }
    
    /**
     * Find top students by loyalty points
     */
    public List<Student> findTopStudentsByLoyaltyPoints(int limit) {
        return findAllStudents().stream()
                .sorted((s1, s2) -> Integer.compare(s2.getLoyaltyPoints(), s1.getLoyaltyPoints()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}