package com.openclassrooms.controllers;

import com.openclassrooms.model.DBUser;
import com.openclassrooms.services.DBUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private DBUserService dbUserService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");
    
        logger.debug("Login attempt for user: {}", email);
        DBUser user = dbUserService.findByEmail(email);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            logger.debug("Invalid email or password for user: {}", email);
            return ResponseEntity.status(401).body("Invalid email or password");
        }
        String token = dbUserService.generateToken(user);
        logger.debug("Login successful for user: {}", email);
        return ResponseEntity.ok(token);
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody DBUser user) {
        try {
            logger.debug("Register attempt for user: {}", user.getEmail());
            String token = dbUserService.registerUser(user);
            logger.debug("Register successful for user: {}", user.getEmail());
            return ResponseEntity.ok(token);
        } catch (IllegalArgumentException e) {
            logger.debug("Register failed for user: {}", user.getEmail(), e);
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestParam String email) {
        logger.debug("Get current user attempt for email: {}", email);
        DBUser user = dbUserService.findByEmail(email);
        if (user == null) {
            logger.debug("User not found with email: {}", email);
            throw new UsernameNotFoundException("User not found");
        }
        logger.debug("User found with email: {}", email);
        return ResponseEntity.ok(user);
    }
}