package com.openclassrooms.services;

import com.openclassrooms.dto.CreateMessageDTO;
import com.openclassrooms.dto.MessageDTO;
import com.openclassrooms.dto.RentalDTO;
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
        RentalDTO rentalDTO = rentalService.findById(createMessageDTO.getRental_id());

        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        if (rentalDTO == null) {
            throw new IllegalArgumentException("Rental not found");
        }

        Rental rental = convertToEntity(rentalDTO);

        Message message = new Message();
        message.setMessage(createMessageDTO.getMessage());
        message.setUserId(createMessageDTO.getUser_id());
        message.setRentalId(createMessageDTO.getRental_id());
        message.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        message.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        try {
            messageRepository.save(message);
        } catch (Exception e) {
            throw new RuntimeException("Error saving message: " + e.getMessage());
        }

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

    private Rental convertToEntity(RentalDTO dto) {
        Rental rental = new Rental();
        rental.setId(dto.getId());
        rental.setName(dto.getName());
        rental.setSurface(dto.getSurface());
        rental.setPrice(dto.getPrice());
        rental.setPicture(dto.getPicture().get(0));
        rental.setDescription(dto.getDescription());
        rental.setOwner(dbUserService.findById(dto.getOwner_id().intValue()));
        rental.setCreatedAt(dto.getCreatedAt());
        rental.setUpdatedAt(dto.getUpdatedAt());
        return rental;
    }
}