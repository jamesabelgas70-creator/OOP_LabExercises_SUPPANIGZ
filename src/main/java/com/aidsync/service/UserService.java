package com.aidsync.service;

import com.aidsync.dao.UserDAO;
import com.aidsync.model.User;

import java.util.List;

/**
 * Service layer for User operations
 */
public class UserService {
    private final UserDAO userDAO;
    
    public UserService() {
        this.userDAO = new UserDAO();
    }
    
    /**
     * Authenticate user
     */
    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        
        return userDAO.authenticate(username.trim(), password);
    }
    
    /**
     * Create new user (Admin only)
     */
    public boolean createUser(User user) {
        return createUser(user, "System");
    }
    
    /**
     * Create new user with activity logging
     */
    public boolean createUser(User user, String createdBy) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (user.getRole() == null || (!user.getRole().equals("Admin") && !user.getRole().equals("Staff"))) {
            throw new IllegalArgumentException("Role must be Admin or Staff");
        }
        
        if (userDAO.usernameExists(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        boolean success = userDAO.createUser(user);
        if (success) {
            ActivityLogService.logActivity(createdBy, "CREATE_USER", 
                "Created " + user.getRole() + " user: " + user.getUsername());
        }
        return success;
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(int id) {
        return userDAO.getUserById(id);
    }
    
    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }
    
    /**
     * Update user
     */
    public boolean updateUser(User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (user.getRole() == null || (!user.getRole().equals("Admin") && !user.getRole().equals("Staff"))) {
            throw new IllegalArgumentException("Role must be Admin or Staff");
        }
        
        if (userDAO.usernameExists(user.getUsername(), user.getId())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        return userDAO.updateUser(user);
    }
    
    /**
     * Delete user with last admin protection
     */
    public boolean deleteUser(int userId) {
        return deleteUser(userId, "System");
    }
    
    /**
     * Delete user with last admin protection and activity logging
     */
    public boolean deleteUser(int userId, String deletedBy) {
        User userToDelete = userDAO.getUserById(userId);
        if (userToDelete != null && userToDelete.isAdmin()) {
            long adminCount = userDAO.getAllUsers().stream()
                .filter(User::isAdmin)
                .count();
            
            if (adminCount <= 1) {
                throw new IllegalArgumentException("Cannot delete the last admin account. Create another admin first.");
            }
        }
        
        boolean success = userDAO.deleteUser(userId);
        if (success && userToDelete != null) {
            ActivityLogService.logActivity(deletedBy, "DELETE_USER", 
                "Deleted " + userToDelete.getRole() + " user: " + userToDelete.getUsername());
        }
        return success;
    }
    
    /**
     * Check if user can be deleted (for UI state management)
     */
    public boolean canDeleteUser(int userId) {
        User user = userDAO.getUserById(userId);
        if (user != null && user.isAdmin()) {
            long adminCount = userDAO.getAllUsers().stream()
                .filter(User::isAdmin)
                .count();
            return adminCount > 1;
        }
        return true; // Staff users can always be deleted
    }
    
    /**
     * Get admin count
     */
    public long getAdminCount() {
        return userDAO.getAllUsers().stream()
            .filter(User::isAdmin)
            .count();
    }
    
    /**
     * Update password
     */
    public boolean updatePassword(int userId, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        
        return userDAO.updatePassword(userId, newPassword);
    }
}