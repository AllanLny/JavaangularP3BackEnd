package com.openclassrooms.services;

import com.openclassrooms.dto.RentalDTO;
import com.openclassrooms.model.DBUser;
import com.openclassrooms.model.Rental;
import com.openclassrooms.repository.RentalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RentalService {

    public static final Path IMAGE_DIR = Paths.get("uploads");

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private DBUserService dbUserService;

    public RentalDTO saveRental(String name, int surface, int price, String description, MultipartFile picture, DBUser owner) throws Exception {
        try {
            Rental rental = new Rental();
            rental.setName(name);
            rental.setSurface((double) surface);
            rental.setPrice((double) price);
            rental.setDescription(description);
            rental.setOwner(owner);
            rental.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            rental.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

            if (!picture.isEmpty()) {
                String pictureFilename = System.currentTimeMillis() + "_" + picture.getOriginalFilename();
                Path picturePath = IMAGE_DIR.resolve(pictureFilename);

                if (!Files.exists(picturePath.getParent())) {
                    Files.createDirectories(picturePath.getParent());
                }

                Files.copy(picture.getInputStream(), picturePath);
                String pictureUrl = picturePath.toUri().toString();
                rental.setPicture(pictureUrl);
            }

            rentalRepository.save(rental);
            return convertToDTO(rental);
        } catch (Exception e) {
            throw new Exception("Error saving rental: " + e.getMessage());
        }
    }

    public List<RentalDTO> findAll() {
        try {
            return rentalRepository.findAll().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving rentals: " + e.getMessage());
        }
    }

    public RentalDTO findById(Long id) {
        return rentalRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new IllegalArgumentException("Rental not found"));
    }

    public RentalDTO updateRental(Long id, String name, Double surface, Double price, String description) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rental not found"));

        rental.setName(name);
        rental.setSurface(surface);
        rental.setPrice(price);
        rental.setDescription(description);
        rental.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        rentalRepository.save(rental);
        return convertToDTO(rental);
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