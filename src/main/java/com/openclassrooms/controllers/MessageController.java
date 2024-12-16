package com.openclassrooms.controllers;

import com.openclassrooms.model.DBUser;
import com.openclassrooms.model.Message;
import com.openclassrooms.model.Rental;
import com.openclassrooms.services.MessageService;
import com.openclassrooms.services.DBUserService;
import com.openclassrooms.services.RentalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DBUserService dbUserService;

    @Autowired
    private RentalService rentalService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @PostMapping
    public ResponseEntity<?> sendMessage(@RequestHeader("Authorization") String token, @RequestBody Message message) {
        // Supprimer le préfixe "Bearer " du token
        String jwtToken = token.replace("Bearer ", "");

        // Décoder le token JWT pour extraire les claims
        Jwt jwt = jwtDecoder.decode(jwtToken);
        String email = jwt.getClaim("sub");

        logger.debug("Decoded email from JWT: {}", email);

        DBUser user = dbUserService.findByEmail(email);
        if (user == null) {
            logger.debug("User not found for email: {}", email);
            return ResponseEntity.status(404).body("User not found");
        }

        logger.debug("Found user: {}", user);

        // Log the received message object
        logger.debug("Message object received: {}", message);

        if (message.getRentalId() == null) {
            logger.debug("Rental ID is null in the message object");
            return ResponseEntity.status(400).body("Rental ID is required");
        }

        Long rentalId = message.getRentalId();
        logger.debug("Rental ID from message: {}", rentalId);

        Rental rental = rentalService.findById(rentalId);
        if (rental == null) {
            logger.debug("Rental not found for ID: {}", rentalId);
            return ResponseEntity.status(404).body("Rental not found");
        }

        logger.debug("Found rental: {}", rental);

        message.setUser(user);
        message.setRental(rental);

        messageService.sendMessage(message);
        logger.debug("Message sent successfully");

        return ResponseEntity.ok(Map.of("message", "Message send with success"));
    }
}