package com.university.cafeteria.service.impl;

import com.university.cafeteria.model.User;
import com.university.cafeteria.model.Student;
import com.university.cafeteria.model.Staff;
import com.university.cafeteria.repository.UserRepository;
import com.university.cafeteria.service.AuthenticationService;
import com.university.cafeteria.service.NotificationService;

import java.util.Optional;
import java.util.regex.Pattern;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Implementation of AuthenticationService
 * Demonstrates Single Responsibility Principle - handles only authentication logic
 * Uses Dependency Injection for UserRepository and NotificationService
 */
public class AuthenticationServiceImpl implements AuthenticationService {
    
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    
    // Validation patterns
    private static final Pattern USER_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^[0-9]{6,10}$");
    private static final Pattern EMPLOYEE_ID_PATTERN = Pattern.compile("^EMP[0-9]{3,6}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s]{2,50}$");
    
    // Minimum password requirements
    private static final int MIN_PASSWORD_LENGTH = 6;
    
    public AuthenticationServiceImpl(UserRepository userRepository, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }
    
    @Override
    public Student registerStudent(String userId, String name, String password, String studentId) {
        // Validate input parameters
        validateStudentRegistrationInput(userId, name, password, studentId);
        
        // Check if IDs are available
        if (!isUserIdAvailable(userId)) {
            throw new IllegalArgumentException("User ID already exists: " + userId);
        }
        
        if (!isStudentIdAvailable(studentId)) {
            throw new IllegalArgumentException("Student ID already exists: " + studentId);
        }
        
        // Create and save student
        Student student = new Student(userId, name.trim(), password, studentId);
        Student savedStudent = (Student) userRepository.save(student);
        
        // Send welcome notification
        notificationService.notifyStudentWelcome(savedStudent);
        // Write-through to DB (best-effort)
        persistStudentToDatabase(savedStudent, password);
        
        return savedStudent;
    }
    
    @Override
    public Staff registerStaff(String userId, String name, String password, String employeeId, Staff.StaffRole role) {
        // Validate input parameters
        validateStaffRegistrationInput(userId, name, password, employeeId, role);
        
        // Check if IDs are available
        if (!isUserIdAvailable(userId)) {
            throw new IllegalArgumentException("User ID already exists: " + userId);
        }
        
        if (!isEmployeeIdAvailable(employeeId)) {
            throw new IllegalArgumentException("Employee ID already exists: " + employeeId);
        }
        
        // Create and save staff
        Staff staff = new Staff(userId, name.trim(), password, employeeId, role);
        Staff saved = (Staff) userRepository.save(staff);
        // Write-through to DB (best-effort)
        persistStaffToDatabase(saved, password);
        return saved;
    }
    
    @Override
    public Optional<User> login(String userId, String password) {
        if (userId == null || password == null) {
            return Optional.empty();
        }
        
        // Try username first
        Optional<User> userOpt = userRepository.findById(userId.trim());
        if (userOpt.isPresent() && userOpt.get().verifyPassword(password)) {
            return userOpt;
        }

        // Also allow login using studentId for convenience
        Optional<Student> studentOpt = userRepository.findStudentByStudentId(userId.trim());
        if (studentOpt.isPresent() && studentOpt.get().verifyPassword(password)) {
            return Optional.of(studentOpt.get());
        }

        return Optional.empty();
    }
    
    @Override
    public boolean validateCredentials(String userId, String password) {
        return login(userId, password).isPresent();
    }
    
    @Override
    public boolean isUserIdAvailable(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        return !userRepository.existsById(userId.trim());
    }
    
    @Override
    public boolean isStudentIdAvailable(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            return false;
        }
        return !userRepository.existsByStudentId(studentId.trim());
    }
    
    @Override
    public boolean isEmployeeIdAvailable(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return false;
        }
        return !userRepository.existsByEmployeeId(employeeId.trim());
    }
    
    @Override
    public Optional<User> findUserById(String userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return userRepository.findById(userId.trim());
    }
    
    @Override
    public Optional<Student> findStudentByStudentId(String studentId) {
        if (studentId == null) {
            return Optional.empty();
        }
        return userRepository.findStudentByStudentId(studentId.trim());
    }
    
    @Override
    public boolean updatePassword(String userId, String oldPassword, String newPassword) {
        if (userId == null || oldPassword == null || newPassword == null) {
            return false;
        }
        
        Optional<User> userOpt = userRepository.findById(userId.trim());
        if (!userOpt.isPresent()) {
            return false;
        }
        
        User user = userOpt.get();
        if (!user.verifyPassword(oldPassword)) {
            return false;
        }
        
        if (!isValidPassword(newPassword)) {
            throw new IllegalArgumentException("New password does not meet requirements");
        }
        
        // Note: In a real application, you would update the password through a setter method
        // For this demo, we'll assume the User class has a method to update password
        // user.updatePassword(newPassword);
        // userRepository.update(user);
        
        return true;
    }
    
    /**
     * Validation helper methods
     */
    
    private void validateStudentRegistrationInput(String userId, String name, String password, String studentId) {
        if (userId == null || !USER_ID_PATTERN.matcher(userId.trim()).matches()) {
            throw new IllegalArgumentException("Invalid user ID. Must be 3-20 characters (letters, numbers, underscore only)");
        }
        
        if (name == null || !NAME_PATTERN.matcher(name.trim()).matches()) {
            throw new IllegalArgumentException("Invalid name. Must be 2-50 characters (letters and spaces only)");
        }
        
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("Invalid password. Must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        
        if (studentId == null || !STUDENT_ID_PATTERN.matcher(studentId.trim()).matches()) {
            throw new IllegalArgumentException("Invalid student ID. Must be 6-10 digits");
        }
    }
    
    private void validateStaffRegistrationInput(String userId, String name, String password, String employeeId, Staff.StaffRole role) {
        if (userId == null || !USER_ID_PATTERN.matcher(userId.trim()).matches()) {
            throw new IllegalArgumentException("Invalid user ID. Must be 3-20 characters (letters, numbers, underscore only)");
        }
        
        if (name == null || !NAME_PATTERN.matcher(name.trim()).matches()) {
            throw new IllegalArgumentException("Invalid name. Must be 2-50 characters (letters and spaces only)");
        }
        
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("Invalid password. Must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        
        if (employeeId == null || !EMPLOYEE_ID_PATTERN.matcher(employeeId.trim()).matches()) {
            throw new IllegalArgumentException("Invalid employee ID. Must follow format EMP### (3-6 digits)");
        }
        
        if (role == null) {
            throw new IllegalArgumentException("Staff role cannot be null");
        }
    }
    
    private boolean isValidPassword(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }
    
    /**
     * Additional utility methods for user management
     */
    
    public long getTotalUserCount() {
        return userRepository.findAllStudents().size() + userRepository.findAllStaff().size();
    }
    
    public long getStudentCount() {
        return userRepository.findAllStudents().size();
    }
    
    public long getStaffCount() {
        return userRepository.findAllStaff().size();
    }
    
    /**
     * Generate suggested user ID based on name
     */
    public String generateSuggestedUserId(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }
        
        // Create base ID from name
        String baseId = name.trim().toLowerCase()
                .replaceAll("[^a-zA-Z0-9]", "")
                .substring(0, Math.min(10, name.length()));
        
        // If base ID is available, return it
        if (isUserIdAvailable(baseId)) {
            return baseId;
        }
        
        // Try with numbers appended
        for (int i = 1; i <= 999; i++) {
            String suggestedId = baseId + i;
            if (isUserIdAvailable(suggestedId)) {
                return suggestedId;
            }
        }
        
        return baseId + System.currentTimeMillis() % 1000;
    }

    /**
     * --- Minimal JDBC write-through helpers for user persistence ---
     */
    private void persistStudentToDatabase(Student student, String rawPassword) {
        String url = System.getProperty("cafeteria.db.url");
        String user = System.getProperty("cafeteria.db.user");
        String pass = System.getProperty("cafeteria.db.password");
        if (url == null || url.isEmpty()) return; // DB not configured
        boolean mysql = url.startsWith("jdbc:mysql:");

        String upsertUser = mysql
                ? "INSERT INTO users (user_id, name, password, user_type, created_at) VALUES (?,?,?,?,?) " +
                  "ON DUPLICATE KEY UPDATE name=VALUES(name), password=VALUES(password), user_type=VALUES(user_type), created_at=VALUES(created_at)"
                : "MERGE INTO users (user_id, name, password, user_type, created_at) KEY(user_id) VALUES (?,?,?,?,?)";

        String upsertStudent = mysql
                ? "INSERT INTO students (student_id, user_id) VALUES (?,?) ON DUPLICATE KEY UPDATE user_id=VALUES(user_id)"
                : "MERGE INTO students (student_id, user_id) KEY(student_id) VALUES (?,?)";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            try (PreparedStatement ps = conn.prepareStatement(upsertUser)) {
                ps.setString(1, student.getUserId());
                ps.setString(2, student.getName());
                ps.setString(3, rawPassword);
                ps.setString(4, "STUDENT");
                ps.setTimestamp(5, Timestamp.valueOf(student.getCreatedAt()));
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(upsertStudent)) {
                ps.setString(1, student.getStudentId());
                ps.setString(2, student.getUserId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[DB] Failed to persist student '" + student.getUserId() + "': " + e.getMessage());
        }
    }

    private void persistStaffToDatabase(Staff staff, String rawPassword) {
        String url = System.getProperty("cafeteria.db.url");
        String user = System.getProperty("cafeteria.db.user");
        String pass = System.getProperty("cafeteria.db.password");
        if (url == null || url.isEmpty()) return;
        boolean mysql = url.startsWith("jdbc:mysql:");

        String upsertUser = mysql
                ? "INSERT INTO users (user_id, name, password, user_type, created_at) VALUES (?,?,?,?,?) " +
                  "ON DUPLICATE KEY UPDATE name=VALUES(name), password=VALUES(password), user_type=VALUES(user_type), created_at=VALUES(created_at)"
                : "MERGE INTO users (user_id, name, password, user_type, created_at) KEY(user_id) VALUES (?,?,?,?,?)";
        String upsertStaff = mysql
                ? "INSERT INTO staff (employee_id, user_id, role) VALUES (?,?,?) ON DUPLICATE KEY UPDATE user_id=VALUES(user_id), role=VALUES(role)"
                : "MERGE INTO staff (employee_id, user_id, role) KEY(employee_id) VALUES (?,?,?)";

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            try (PreparedStatement ps = conn.prepareStatement(upsertUser)) {
                ps.setString(1, staff.getUserId());
                ps.setString(2, staff.getName());
                ps.setString(3, rawPassword);
                ps.setString(4, "STAFF");
                ps.setTimestamp(5, Timestamp.valueOf(staff.getCreatedAt()));
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(upsertStaff)) {
                ps.setString(1, staff.getEmployeeId());
                ps.setString(2, staff.getUserId());
                ps.setString(3, staff.getRole().name());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[DB] Failed to persist staff '" + staff.getUserId() + "': " + e.getMessage());
        }
    }
}