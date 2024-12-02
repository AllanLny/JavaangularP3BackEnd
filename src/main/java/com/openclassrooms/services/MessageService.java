package com.openclassrooms.services;

import com.openclassrooms.model.Message;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    public Message sendMessage(String messageContent) {
        Message message = new Message();
        message.setMessage(messageContent);
        return message;
    }
}