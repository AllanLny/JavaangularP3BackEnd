package com.openclassrooms.controllers;

import com.openclassrooms.dto.RentalDTO;
import com.openclassrooms.dto.RentalResponse;
import com.openclassrooms.model.DBUser;
import com.openclassrooms.model.Rental;
import com.openclassrooms.services.RentalService;
import com.openclassrooms.services.DBUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    private static final Logger logger = LoggerFactory.getLogger(RentalController.class);
    private static final Path IMAGE_DIR = Paths.get("uploads");

    @Autowired
    private RentalService rentalService;

    @Autowired
    private DBUserService dbUserService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a new rental")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created rental"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Invalid JWT token")
    })
    public ResponseEntity<RentalResponse> createRental(
            @RequestHeader("Authorization") String token,
            @RequestParam("name") String name,
            @RequestParam("surface") int surface,
            @RequestParam("price") int price,
            @RequestParam("description") String description,
            @RequestParam("picture") MultipartFile picture) {
        String jwtToken = token.replace("Bearer ", "");

        try {
            Jwt jwt = jwtDecoder.decode(jwtToken);
            String email = jwt.getClaim("sub");

            logger.debug("Create rental attempt by user with email: {}", email);

            DBUser owner = dbUserService.findByEmail(email);
            if (owner == null) {
                return ResponseEntity.status(404).body(null);
            }

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
                    logger.debug("Created directory: {}", picturePath.getParent().toAbsolutePath());
                }

                Files.copy(picture.getInputStream(), picturePath);
                logger.debug("Saved picture to: {}", picturePath.toAbsolutePath());
                String pictureUrl = "http://localhost:3001/api/rentals/images/" + pictureFilename;
                rental.setPicture(pictureUrl);
            }

            rentalService.save(rental);

            RentalDTO rentalDTO = new RentalDTO();
            rentalDTO.setId(rental.getId());
            rentalDTO.setName(rental.getName());
            rentalDTO.setSurface(rental.getSurface());
            rentalDTO.setPrice(rental.getPrice());
            rentalDTO.setDescription(rental.getDescription());
            rentalDTO.setPicture(Collections.singletonList(rental.getPicture()));
            rentalDTO.setCreatedAt(rental.getCreatedAt());
            rentalDTO.setUpdatedAt(rental.getUpdatedAt());

            RentalResponse response = new RentalResponse();
            response.setMessage("Rental created successfully");
            response.setRental(rentalDTO);

            return ResponseEntity.ok(response);
        } catch (JwtException e) {
            logger.debug("Invalid JWT token: {}", e.getMessage());
            return ResponseEntity.status(401).body(null);
        } catch (Exception e) {
            logger.error("Error creating rental: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping
    @Operation(summary = "View a list of available rentals")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "401", description = "Invalid JWT token")
    })
    public ResponseEntity<RentalResponse> getAllRentals(@RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");

        try {
            Jwt jwt = jwtDecoder.decode(jwtToken);
            String email = jwt.getClaim("sub");

            List<RentalDTO> rentals = rentalService.findAll();

            // Ajout de messages de débogage pour chaque location
            for (RentalDTO rental : rentals) {
                logger.debug("Rental ID: {}", rental.getId());
                logger.debug("Rental Name: {}", rental.getName());
                logger.debug("Rental Surface: {}", rental.getSurface());
                logger.debug("Rental Price: {}", rental.getPrice());
                logger.debug("Rental Description: {}", rental.getDescription());
                logger.debug("Rental Owner ID: {}", rental.getOwner_id());
                logger.debug("Rental Created At: {}", rental.getCreatedAt());
                logger.debug("Rental Updated At: {}", rental.getUpdatedAt());
                logger.debug("Rental Pictures: {}", rental.getPicture());
            }

            RentalResponse response = new RentalResponse();
            response.setRentals(rentals);

            return ResponseEntity.ok(response);
        } catch (JwtException e) {
            return ResponseEntity.status(401).body(null);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a rental by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved rental"),
            @ApiResponse(responseCode = "404", description = "Rental not found")
    })
    public ResponseEntity<RentalDTO> getRentalById(@PathVariable Long id) {
        RentalDTO rentalDTO = rentalService.findById(id);
        if (rentalDTO == null) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(rentalDTO);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update rental")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated rental"),
            @ApiResponse(responseCode = "404", description = "Rental not found"),
            @ApiResponse(responseCode = "401", description = "Invalid JWT token"),
            @ApiResponse(responseCode = "500", description = "Error updating rental")
    })
    public ResponseEntity<RentalDTO> updateRental(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("surface") Double surface,
            @RequestParam("price") Double price,
            @RequestParam("description") String description,
            @RequestHeader("Authorization") String token) {
        try {
            Rental rental = rentalService.findEntityById(id);
            if (rental == null) {
                return ResponseEntity.status(404).body(null);
            }
    
            // Mettre à jour les champs du rental
            rental.setName(name);
            rental.setSurface(surface);
            rental.setPrice(price);
            rental.setDescription(description);
    
            rental.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            rentalService.save(rental);
    
            RentalDTO updatedRentalDTO = new RentalDTO();
            updatedRentalDTO.setId(rental.getId());
            updatedRentalDTO.setName(rental.getName());
            updatedRentalDTO.setSurface(rental.getSurface());
            updatedRentalDTO.setPrice(rental.getPrice());
            updatedRentalDTO.setDescription(rental.getDescription());
            updatedRentalDTO.setPicture(Collections.singletonList(rental.getPicture()));
            updatedRentalDTO.setCreatedAt(rental.getCreatedAt());
            updatedRentalDTO.setUpdatedAt(rental.getUpdatedAt());
    
            return ResponseEntity.ok(updatedRentalDTO);
        } catch (JwtException e) {
            logger.debug("Invalid JWT token: {}", e.getMessage());
            return ResponseEntity.status(401).body(null);
        } catch (Exception e) {
            logger.error("Error updating rental: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/images/{filename:.+}")
    @Operation(summary = "Serve rental image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully served image"),
            @ApiResponse(responseCode = "404", description = "Image not found")
    })
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) throws MalformedURLException {
        Path file = IMAGE_DIR.resolve(filename);
        Resource resource = new UrlResource(file.toUri());
        if (resource.exists() || resource.isReadable()) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } else {
            return ResponseEntity.status(404).body(null);
        }
    }
}