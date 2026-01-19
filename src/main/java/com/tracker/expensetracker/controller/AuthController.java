package com.tracker.expensetracker.controller;

import com.tracker.expensetracker.dto.AuthResponse;
import com.tracker.expensetracker.entity.User;
import com.tracker.expensetracker.security.JwtUtils;
import com.tracker.expensetracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    // Register Endpoint
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User newUser = userService.registerUser(user);
            return ResponseEntity.ok(newUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Login Endpoint
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        // 1. Ask Spring Security: "Are these credentials correct?"
        Authentication authentication = authenticationManager.authenticate(
                // The app uses email as the login identifier
                new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
        );

        // 2. If valid, set the user in the context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Generate the Token
        // Store the email in the token subject so downstream can resolve the user
        String jwt = jwtUtils.generateToken(user.getEmail());

        // 4. Return the Token to the user
        return ResponseEntity.ok(new AuthResponse(jwt));
    }
}