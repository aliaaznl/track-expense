package com.tracker.expensetracker.service;

import com.tracker.expensetracker.entity.User;
import com.tracker.expensetracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Inject the Encryptor
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private BudgetRepository budgetRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ExportRepository exportRepository;
    
    @Autowired
    private EmailService emailService;

    public User registerUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already taken!");
        }

        // ENCRYPT PASSWORD BEFORE SAVING
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    // Get user by email
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Update user currency
    public User updateCurrency(String email, String currency) {
        User user = getUserByEmail(email);
        user.setCurrency(currency);
        return userRepository.save(user);
    }

    // Update user profile (username and email)
    public User updateProfile(String email, String username, String newEmail) {
        User user = getUserByEmail(email);
        
        // Check if new email is already taken by another user
        if (newEmail != null && !newEmail.equals(email)) {
            userRepository.findByEmail(newEmail).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(user.getId())) {
                    throw new RuntimeException("Email is already taken");
                }
            });
        }
        
        // Check if new username is already taken by another user
        if (username != null && !username.equals(user.getUsername())) {
            userRepository.findByUsername(username).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(user.getId())) {
                    throw new RuntimeException("Username is already taken");
                }
            });
        }
        
        if (username != null && !username.trim().isEmpty()) {
            user.setUsername(username);
        }
        
        if (newEmail != null && !newEmail.trim().isEmpty()) {
            user.setEmail(newEmail);
        }
        
        return userRepository.save(user);
    }

    // Update user password
    public void updatePassword(String email, String currentPassword, String newPassword) {
        User user = getUserByEmail(email);
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Update to new password (encrypted)
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // Request password reset - generates token and sets expiry
    public void requestPasswordReset(String email) {
        User user = getUserByEmail(email);
        
        // Generate secure random token
        String token = java.util.UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(java.time.LocalDateTime.now().plusHours(1)); // Token valid for 1 hour
        userRepository.save(user);
        
        // Send password reset email
        emailService.sendPasswordResetEmail(email, token);
    }

    // Reset password using token
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));
        
        // Check if token is expired
        if (user.getResetTokenExpiry() == null || 
            user.getResetTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }
        
        // Update password and clear reset token
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    // Delete user account and all associated data
    @Transactional
    public void deleteUser(String email) {
        User user = getUserByEmail(email);
        
        // Delete all user's transactions
        transactionRepository.deleteAll(transactionRepository.findByUser(user));
        
        // Delete all user's budgets
        budgetRepository.deleteAll(budgetRepository.findByUser(user));
        
        // Delete all user's custom categories (system categories are preserved)
        categoryRepository.deleteByUser(user);
        
        // Delete all user's exports
        exportRepository.deleteByUser(user);
        
        // Finally, delete the user account
        userRepository.delete(user);
    }

    // Note: We removed the manual "loginUser" method here because
    // AuthenticationManager will handle password checking for us now!
}
