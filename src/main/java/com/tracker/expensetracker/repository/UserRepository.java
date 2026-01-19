package com.tracker.expensetracker.repository;

import com.tracker.expensetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Used for Login (finding by email)
    Optional<User> findByEmail(String email);

    // --- ADD THIS BACK (Used for Registration check) ---
    Optional<User> findByUsername(String username);
    
    // Used for password reset
    Optional<User> findByResetToken(String resetToken);
}