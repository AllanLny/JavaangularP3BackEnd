package com.openclassrooms.controllers;

import com.openclassrooms.dto.CreateMessageDTO;
import com.openclassrooms.dto.MessageDTO;
import com.openclassrooms.dto.MessageResponse;
import com.openclassrooms.model.DBUser;
import com.openclassrooms.services.DBUserService;
import com.openclassrooms.services.MessageService;
import com.openclassrooms.services.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private DBUserService dbUserService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private RentalService rentalService;

    @Autowired
    private MessageService messageService;

    @PostMapping
    @Operation(summary = "Create a new message")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created message",
                         content = @Content(schema = @Schema(implementation = MessageDTO.class))),
            @ApiResponse(responseCode = "404", description = "User or rental not found")
    })
    public ResponseEntity<MessageResponse> createMessage(@RequestHeader("Authorization") String token, @RequestBody CreateMessageDTO createMessageDTO) {
        String jwtToken = token.replace("Bearer ", "");
        Jwt jwt = jwtDecoder.decode(jwtToken);
        String email = jwt.getClaim("sub");

        dbUserService.findByEmail(email);
        messageService.saveMessage(createMessageDTO);

        MessageResponse response = new MessageResponse("Message send with success");
        return ResponseEntity.ok(response);
    }
}