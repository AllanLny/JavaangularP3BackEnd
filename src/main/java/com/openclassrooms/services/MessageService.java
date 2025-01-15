package com.openclassrooms.services;

import com.openclassrooms.dto.CreateMessageDTO;
import com.openclassrooms.dto.MessageDTO;
import com.openclassrooms.model.DBUser;
import com.openclassrooms.model.Message;
import com.openclassrooms.model.Rental;
import com.openclassrooms.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private DBUserService dbUserService;

    @Autowired
    private RentalService rentalService;

    public MessageDTO saveMessage(CreateMessageDTO createMessageDTO) {
        DBUser user = dbUserService.findById(createMessageDTO.getUser_id().intValue());
        Rental rental = rentalService.findEntityById(createMessageDTO.getRental_id());

        if (user == null || rental == null) {
            return null;
        }

        Message message = new Message();
        message.setMessage(createMessageDTO.getMessage());
        message.setUserId(createMessageDTO.getUser_id());
        message.setRentalId(createMessageDTO.getRental_id());
        message.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        message.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        messageRepository.save(message);
        return convertToDTO(message);
    }

    private MessageDTO convertToDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setMessage(message.getMessage());
        dto.setUser_id(message.getUserId());
        dto.setRental_id(message.getRentalId());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setUpdatedAt(message.getUpdatedAt());
        return dto;
    }
}