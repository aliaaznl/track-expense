package com.tracker.expensetracker.controller;

import com.tracker.expensetracker.entity.User;
import com.tracker.expensetracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    // GET current user info
    @GetMapping
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("UserController: Authentication object: " + auth);
            System.out.println("UserController: Authentication name: " + (auth != null ? auth.getName() : "null"));
            System.out.println("UserController: Is authenticated: " + (auth != null && auth.isAuthenticated()));
            
            if (auth == null || auth.getName() == null || auth.getName().equals("anonymousUser")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Not authenticated");
                System.out.println("UserController: Returning 401 - Not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            String email = auth.getName();
            System.out.println("UserController: Fetching user with email: " + email);
            User user = userService.getUserByEmail(email);
            // Don't send password
            user.setPassword(null);
            
            System.out.println("UserController: Successfully fetched user: " + user.getEmail());
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            System.out.println("UserController: RuntimeException - " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            System.out.println("UserController: Exception - " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // PUT update currency
    @PutMapping("/currency")
    public ResponseEntity<User> updateCurrency(@RequestBody Map<String, String> request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        String currency = request.get("currency");
        if (currency == null || currency.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        User user = userService.updateCurrency(email, currency);
        user.setPassword(null);
        
        return ResponseEntity.ok(user);
    }

    // PUT update profile (username and email)
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth == null || auth.getName() == null || auth.getName().equals("anonymousUser")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            String email = auth.getName();
            String username = request.get("username");
            String newEmail = request.get("email");
            
            if (username == null && newEmail == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Username or email is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            User user = userService.updateProfile(email, username, newEmail);
            user.setPassword(null);
            
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update profile");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // PUT update password
    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth == null || auth.getName() == null || auth.getName().equals("anonymousUser")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            String email = auth.getName();
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            
            if (currentPassword == null || newPassword == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Current password and new password are required");
                return ResponseEntity.badRequest().body(error);
            }
            
            userService.updatePassword(email, currentPassword, newPassword);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password updated successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update password");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // POST request password reset
    @PostMapping("/password/reset-request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Email is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            userService.requestPasswordReset(email);
            
            // Always return success message (don't reveal if email exists)
            Map<String, String> response = new HashMap<>();
            response.put("message", "If an account with that email exists, a password reset link has been sent.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // User not found - still return success for security
            Map<String, String> response = new HashMap<>();
            response.put("message", "If an account with that email exists, a password reset link has been sent.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to process reset request");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // POST reset password with token
    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");
            
            if (token == null || newPassword == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Token and new password are required");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (newPassword.length() < 6) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Password must be at least 6 characters long");
                return ResponseEntity.badRequest().body(error);
            }
            
            userService.resetPassword(token, newPassword);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password has been reset successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to reset password");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // DELETE user account
    @DeleteMapping
    public ResponseEntity<?> deleteUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth == null || auth.getName() == null || auth.getName().equals("anonymousUser")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            String email = auth.getName();
            userService.deleteUser(email);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Account deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete account");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
