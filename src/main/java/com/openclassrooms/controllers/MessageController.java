package com.openclassrooms.controllers;

import com.openclassrooms.model.Message;
import com.openclassrooms.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping
    public ResponseEntity<Message> sendMessage(@RequestBody Message message) {
        Message responseMessage = messageService.sendMessage(message.getMessage());
        return ResponseEntity.ok(responseMessage);
    }
}