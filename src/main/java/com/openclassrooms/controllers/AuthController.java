package com.openclassrooms.controllers;

import com.openclassrooms.model.DBUser;
import com.openclassrooms.services.DBUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private DBUserService dbUserService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @PostMapping("/login")
    @Operation(summary = "Login a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully logged in"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        DBUser user = dbUserService.authenticate(email, password);
        if (user == null) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        String token = dbUserService.generateToken(user);
        logger.debug("Login successful for user: {}", email);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<?> register(@RequestBody DBUser user) {
        try {
            logger.debug("Register attempt for user: {}", user.getEmail());
            String token = dbUserService.registerUser(user);
            logger.debug("Register successful for user: {}", user.getEmail());
            return ResponseEntity.ok(Map.of("token", token));
        } catch (IllegalArgumentException e) {
            logger.debug("Register failed for user: {}", user.getEmail(), e);
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user"),
            @ApiResponse(responseCode = "401", description = "Invalid JWT token"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");

        try {
            Jwt jwt = jwtDecoder.decode(jwtToken);
            String email = jwt.getClaim("sub");

            logger.debug("Get current user attempt for email: {}", email);
            DBUser user = dbUserService.findByEmail(email);
            if (user == null) {
                logger.debug("User not found with email: {}", email);
                throw new UsernameNotFoundException("User not found");
            }
            logger.debug("User found with email: {}", email);
            Map<String, Object> userMap = convertUserToMap(user);
            return ResponseEntity.ok(userMap);
        } catch (JwtException e) {
            logger.debug("Invalid JWT token: {}", e.getMessage());
            return ResponseEntity.status(401).body("Invalid JWT token");
        }
    }

    private Map<String, Object> convertUserToMap(DBUser user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("email", user.getEmail());
        userMap.put("name", user.getName());
        userMap.put("created_at", user.getFormattedCreatedAt());
        userMap.put("updated_at", user.getFormattedUpdatedAt());
        return userMap;
    }
}