package com.tracker.expensetracker.security;

import com.tracker.expensetracker.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    // --- CORRECTION HERE: This sits at the CLASS level (top), not inside the method ---
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        
        // Skip CORS preflight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 1. Get the Authorization header from the request
        String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        System.out.println("JWT Filter: Processing request - " + request.getMethod() + " " + requestPath);
        System.out.println("JWT Filter: Authorization header present: " + (authHeader != null));
        
        // 2. Check if the header starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            System.out.println("JWT Filter: Token extracted, length: " + jwt.length());
            try {
                username = jwtUtils.getUsernameFromToken(jwt);
                System.out.println("JWT Filter: Username extracted from token: " + username);
            } catch (Exception e) {
                // Invalid token format - log and continue without authentication
                System.out.println("JWT Filter: Failed to extract username from token: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (requestPath.startsWith("/api/")) {
            System.out.println("JWT Filter: API request without valid Authorization header: " + requestPath);
        }

        // 3. If we found a username and the user is not already logged in
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            System.out.println("JWT Filter: Attempting to authenticate user: " + username);
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                System.out.println("JWT Filter: UserDetails loaded successfully");

                // 4. Validate the token
                boolean isValid = jwtUtils.validateToken(jwt);
                System.out.println("JWT Filter: Token validation result: " + isValid);
                
                if (isValid) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("JWT Filter: ✓ Successfully authenticated user: " + username);
                } else {
                    System.out.println("JWT Filter: ✗ Token validation failed for user: " + username);
                }
            } catch (Exception e) {
                // Error loading user - log the error
                System.out.println("JWT Filter: ✗ Error loading user or validating token: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            if (username == null) {
                System.out.println("JWT Filter: No username extracted from token");
            }
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                System.out.println("JWT Filter: User already authenticated: " + 
                    SecurityContextHolder.getContext().getAuthentication().getName());
            }
        }

        // 6. Continue the request chain
        filterChain.doFilter(request, response);
    }
}