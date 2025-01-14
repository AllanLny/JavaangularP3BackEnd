package com.openclassrooms.controllers;

import com.openclassrooms.dto.AuthResponseDTO;
import com.openclassrooms.dto.DBUserDTO;
import com.openclassrooms.dto.LoginRequestDTO;
import com.openclassrooms.dto.RegisterUserDTO;
import com.openclassrooms.model.DBUser;
import com.openclassrooms.services.DBUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private DBUserService dbUserService;

    @Autowired
    private JwtDecoder jwtDecoder;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/login")
    @Operation(summary = "Login a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully logged in"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        AuthResponseDTO response = dbUserService.login(loginRequest.getEmail(), loginRequest.getPassword());
        if (response == null) {
            return ResponseEntity.status(401).body(new AuthResponseDTO(null, "Invalid credentials"));
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User object to be registered",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegisterUserDTO.class))
            ))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully registered",
                    content = @Content(schema = @Schema(implementation = AuthResponseDTO.class)))
    })
    public ResponseEntity<AuthResponseDTO> register(@RequestBody RegisterUserDTO registerUser) {
        DBUser user = new DBUser();
        user.setEmail(registerUser.getEmail());
        user.setName(registerUser.getName());
        user.setPassword(registerUser.getPassword());

        String token = dbUserService.registerUser(user);
        return ResponseEntity.ok(new AuthResponseDTO(token, "Successfully registered"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user",
                    content = @Content(schema = @Schema(implementation = DBUserDTO.class))),
    })
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            logger.debug("Invalid Authorization header");
            return ResponseEntity.status(401).body("Invalid Authorization header");
        }

        String jwtToken = token.replace("Bearer ", "");

        try {
            Jwt jwt = jwtDecoder.decode(jwtToken);
            String email = jwt.getClaim("sub");

            logger.debug("Get current user attempt for email: {}", email);
            DBUser user = dbUserService.findByEmail(email);
            if (user == null) {
                logger.debug("User not found with email: {}", email);
                return ResponseEntity.status(404).body("User not found");
            }
            logger.debug("User found with email: {}", email);
            DBUserDTO userDTO = convertUserToDTO(user);
            return ResponseEntity.ok(userDTO);
        } catch (JwtException e) {
            logger.debug("Invalid JWT token: {}", e.getMessage());
            return ResponseEntity.status(401).body("Invalid JWT token");
        }
    }

    private DBUserDTO convertUserToDTO(DBUser user) {
        DBUserDTO userDTO = new DBUserDTO();
        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setName(user.getName());
        userDTO.setCreatedAt(user.getFormattedCreatedAt());
        userDTO.setUpdatedAt(user.getFormattedUpdatedAt());
        return userDTO;
    }
}