package com.openclassrooms.services;

import com.openclassrooms.dto.RentalDTO;
import com.openclassrooms.model.Rental;
import com.openclassrooms.repository.RentalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RentalService {

    private static final Logger logger = LoggerFactory.getLogger(RentalService.class);

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private DBUserService dbUserService;

    public List<RentalDTO> findAll() {
        return rentalRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public void save(Rental rental) {
        rentalRepository.save(rental);
    }

    public RentalDTO findById(Long id) {
        return rentalRepository.findById(id).map(this::convertToDTO).orElse(null);
    }

    public Rental findEntityById(Long id) {
        logger.debug("Finding rental with id: {}", id);
        return rentalRepository.findById(id).orElse(null);
    }

    private RentalDTO convertToDTO(Rental rental) {
        RentalDTO dto = new RentalDTO();
        dto.setId(rental.getId());
        dto.setName(rental.getName());
        dto.setSurface(rental.getSurface());
        dto.setPrice(rental.getPrice());
        dto.setPicture(Collections.singletonList(rental.getPicture()));
        dto.setDescription(rental.getDescription());
        dto.setOwner_id(rental.getOwner().getId().longValue());
        dto.setCreatedAt(rental.getCreatedAt());
        dto.setUpdatedAt(rental.getUpdatedAt());
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