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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

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
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        AuthResponseDTO response = dbUserService.login(loginRequest.getEmail(), loginRequest.getPassword());
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
                    content = @Content(schema = @Schema(implementation = AuthResponseDTO.class)))
    })
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterUserDTO registerUser) {
        DBUser user = new DBUser();
        user.setEmail(registerUser.getEmail());
        user.setName(registerUser.getName());
        user.setPassword(registerUser.getPassword());

        Map<String, String> tokenResponse = dbUserService.registerUser(user);
        return ResponseEntity.ok(tokenResponse);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user",
                    content = @Content(schema = @Schema(implementation = DBUserDTO.class))),
    })
    public ResponseEntity<DBUserDTO> getCurrentUser(@RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        Jwt jwt = jwtDecoder.decode(jwtToken);
        String email = jwt.getClaim("sub");
        DBUser user = dbUserService.findByEmail(email);
        DBUserDTO userDTO = dbUserService.convertToDTO(user);
        return ResponseEntity.ok(userDTO);
    }
}