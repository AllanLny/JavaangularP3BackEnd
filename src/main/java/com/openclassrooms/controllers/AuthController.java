package com.openclassrooms.controllers;

import com.openclassrooms.dto.AuthResponseDTO;
import com.openclassrooms.dto.DBUserDTO;
import com.openclassrooms.dto.LoginRequestDTO;
import com.openclassrooms.dto.RegisterUserDTO;
import com.openclassrooms.dto.TokenResponseDTO;
import com.openclassrooms.model.DBUser;
import com.openclassrooms.services.DBUserService;
import com.openclassrooms.configuration.JWTUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private DBUserService dbUserService;

    @Autowired
    private JWTUtils jwtUtils;

    @PostMapping("/login")
    @Operation(summary = "Login a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully logged in"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<TokenResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        TokenResponseDTO response = dbUserService.login(loginRequest.getEmail(), loginRequest.getPassword());
        if (response == null) {
            return ResponseEntity.status(401).body(null);
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
                    content = @Content(schema = @Schema(implementation = TokenResponseDTO.class)))
    })
    public ResponseEntity<TokenResponseDTO> register(@Validated @RequestBody RegisterUserDTO registerUser) {
        TokenResponseDTO tokenResponse = dbUserService.registerUser(registerUser);
        return ResponseEntity.ok(tokenResponse);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user",
                    content = @Content(schema = @Schema(implementation = DBUserDTO.class))),
    })
    public ResponseEntity<DBUserDTO> getCurrentUser(@RequestHeader("Authorization") String token) {
        Map<String, Object> claims = jwtUtils.decodeToken(token);
        String email = (String) claims.get("sub");
        DBUser user = dbUserService.findByEmail(email);
        DBUserDTO userDTO = dbUserService.convertToDTO(user);
        return ResponseEntity.ok(userDTO);
    }
}