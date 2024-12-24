package com.openclassrooms.controllers;

import com.openclassrooms.dto.RentalDTO;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<?> createRental(
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
                return ResponseEntity.status(404).body("Owner not found");
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
            return ResponseEntity.ok(Collections.singletonMap("message", "Rental created!"));
        } catch (JwtException e) {
            logger.debug("Invalid JWT token: {}", e.getMessage());
            return ResponseEntity.status(401).body("Invalid JWT token");
        } catch (Exception e) {
            logger.error("Error creating rental: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error creating rental");
        }
    }

    @GetMapping
    @Operation(summary = "View a list of available rentals")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "401", description = "Invalid JWT token")
    })
    public ResponseEntity<?> getAllRentals(@RequestHeader("Authorization") String token) {
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

            Map<String, Object> response = new HashMap<>();
            response.put("rentals", rentals);

            return ResponseEntity.ok(response);
        } catch (JwtException e) {
            return ResponseEntity.status(401).body("Invalid JWT token");
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a rental by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved rental"),
            @ApiResponse(responseCode = "404", description = "Rental not found")
    })
    public ResponseEntity<?> getRentalById(@PathVariable Long id) {
        RentalDTO rentalDTO = rentalService.findById(id);
        if (rentalDTO == null) {
            return ResponseEntity.status(404).body("Rental not found");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", rentalDTO.getId());
        response.put("name", rentalDTO.getName());
        response.put("surface", rentalDTO.getSurface());
        response.put("price", rentalDTO.getPrice());
        response.put("picture", rentalDTO.getPicture());
        response.put("description", rentalDTO.getDescription());
        response.put("owner_id", rentalDTO.getOwner_id());
        response.put("created_at", rentalDTO.getCreatedAt().toString());
        response.put("updated_at", rentalDTO.getUpdatedAt().toString());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a rental")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated rental"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Invalid JWT token"),
            @ApiResponse(responseCode = "404", description = "Rental not found")
    })
    public ResponseEntity<?> updateRental(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token,
            @ModelAttribute RentalDTO rentalDTO) {
        String jwtToken = token.replace("Bearer ", "");

        try {
            Jwt jwt = jwtDecoder.decode(jwtToken);
            String email = jwt.getClaim("sub");

            logger.debug("Update rental attempt by user with email: {}", email);

            Rental rental = rentalService.findEntityById(id);
            if (rental == null) {
                return ResponseEntity.status(404).body("Rental not found");
            }

            // Mettre à jour les champs du rental
            rental.setName(rentalDTO.getName());
            rental.setSurface(rentalDTO.getSurface());
            rental.setPrice(rentalDTO.getPrice());
            rental.setDescription(rentalDTO.getDescription());

            // Mettre à jour l'image si nécessaire
            MultipartFile picture = rentalDTO.getPictureFile();
            if (picture != null && !picture.isEmpty()) {
                String pictureFilename = System.currentTimeMillis() + "_" + picture.getOriginalFilename();
                Path picturePath = IMAGE_DIR.resolve(pictureFilename);
                Files.copy(picture.getInputStream(), picturePath);
                String pictureURL = "http://localhost:3001/api/rentals/images/" + pictureFilename;
                rental.setPicture(pictureURL);
            }

            rental.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            rentalService.save(rental);
            return ResponseEntity.ok(Collections.singletonMap("message", "Rental updated!"));
        } catch (JwtException e) {
            logger.debug("Invalid JWT token: {}", e.getMessage());
            return ResponseEntity.status(401).body("Invalid JWT token");
        } catch (Exception e) {
            logger.error("Error updating rental: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error updating rental");
        }
    }

    @GetMapping("/images/{filename:.+}")
    @Operation(summary = "Serve rental image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully served image"),
            @ApiResponse(responseCode = "404", description = "Image not found"),
            @ApiResponse(responseCode = "500", description = "Error serving image")
    })
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path file = IMAGE_DIR.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.status(404).body(null);
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}