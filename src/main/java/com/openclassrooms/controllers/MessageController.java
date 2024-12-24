package com.openclassrooms.controllers;

import com.openclassrooms.dto.MessageDTO;
import com.openclassrooms.model.Message;
import com.openclassrooms.model.Rental;
import com.openclassrooms.model.DBUser;
import com.openclassrooms.services.MessageService;
import com.openclassrooms.services.RentalService;
import com.openclassrooms.services.DBUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private RentalService rentalService;

    @Autowired
    private DBUserService dbUserService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @PostMapping
    @Operation(summary = "Send a message")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message sent with success"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Rental not found"),
            @ApiResponse(responseCode = "401", description = "Invalid JWT token")
    })
    public ResponseEntity<?> sendMessage(@RequestHeader("Authorization") String token, @RequestBody MessageDTO messageDTO) {
        String jwtToken = token.replace("Bearer ", "");

        try {
            Jwt jwt = jwtDecoder.decode(jwtToken);
            String email = jwt.getClaim("sub");

            logger.debug("Send message attempt by user with email: {}", email);
            DBUser user = dbUserService.findByEmail(email);
            if (user == null) {
                logger.debug("User not found with email: {}", email);
                return ResponseEntity.status(404).body("User not found");
            }

            Long rentalId = messageDTO.getRental_id();
            if (rentalId == null) {
                logger.debug("Rental ID is null in messageDTO");
                return ResponseEntity.status(400).body("Rental ID must not be null");
            }
            logger.debug("Rental ID from messageDTO: {}", rentalId);

            Rental rental = rentalService.findEntityById(rentalId);
            if (rental == null) {
                logger.debug("Rental not found for ID: {}", rentalId);
                return ResponseEntity.status(404).body("Rental not found");
            }

            logger.debug("Found rental: {}", rental);

            Message message = new Message();
            message.setUser(user);
            message.setRental(rental);
            message.setMessage(messageDTO.getMessage());
            message.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            message.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            message.setUserId(messageDTO.getUser_id()); // Ensure user_id is set
            message.setRentalId(messageDTO.getRental_id()); // Ensure rental_id is set

            messageService.sendMessage(message);
            logger.debug("Message sent successfully");

            return ResponseEntity.ok(Map.of("message", "Message sent with success"));
        } catch (JwtException e) {
            logger.debug("Invalid JWT token: {}", e.getMessage());
            return ResponseEntity.status(401).body("Invalid JWT token");
        }
    }
}