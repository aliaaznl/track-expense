package com.tracker.expensetracker.service;

import com.tracker.expensetracker.entity.User;
import com.tracker.expensetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    // IMPORTANT: The method name MUST remain "loadUserByUsername" because of the Interface.
    // Spring passes the login input (email) into this 'username' variable.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 1. Find the user in YOUR database using the Email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // 2. Convert it to a SPRING SECURITY User
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), // We set the email as the "username" for the session
                user.getPassword(),
                new ArrayList<>()
        );
    }
}