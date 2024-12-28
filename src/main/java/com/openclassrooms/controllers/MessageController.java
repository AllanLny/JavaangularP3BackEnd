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
    @Operation(summary = "Create a new message")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created message"),
            @ApiResponse(responseCode = "401", description = "Invalid JWT token"),
            @ApiResponse(responseCode = "404", description = "User or rental not found")
    })
    public ResponseEntity<MessageDTO> createMessage(@RequestHeader("Authorization") String token, @RequestBody MessageDTO messageDTO) {
        String jwtToken = token.replace("Bearer ", "");

        try {
            Jwt jwt = jwtDecoder.decode(jwtToken);
            String email = jwt.getClaim("sub");

            DBUser user = dbUserService.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(null);
            }

            Rental rental = rentalService.findEntityById(messageDTO.getRental_id());
            if (rental == null) {
                return ResponseEntity.status(404).body(null);
            }

            Message message = new Message();
            message.setMessage(messageDTO.getMessage());
            message.setUser(user);
            message.setUserId(user.getId().longValue()); // Set userId
            message.setRental(rental);
            message.setRentalId(rental.getId().longValue()); // Set rentalId
            message.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            message.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

            Message savedMessage = messageService.saveMessage(message);

            MessageDTO responseDTO = convertMessageToDTO(savedMessage);
            responseDTO.setMessage("Message sent successfully");
            return ResponseEntity.ok(responseDTO);
        } catch (JwtException e) {
            return ResponseEntity.status(401).body(null);
        }
    }

    private MessageDTO convertMessageToDTO(Message message) {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setId(message.getId());
        messageDTO.setMessage(message.getMessage());
        messageDTO.setUser_id(message.getUserId());
        messageDTO.setRental_id(message.getRentalId());
        messageDTO.setCreatedAt(message.getCreatedAt());
        messageDTO.setUpdatedAt(message.getUpdatedAt());
        return messageDTO;
    }
}