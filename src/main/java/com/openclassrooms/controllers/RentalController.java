package com.openclassrooms.controllers;

import com.openclassrooms.model.Rental;
import com.openclassrooms.model.DBUser;
import com.openclassrooms.services.RentalService;
import com.openclassrooms.services.DBUserService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    private final Logger logger = LoggerFactory.getLogger(RentalController.class);

    private static final Path IMAGE_DIR = Paths.get("uploads");

    @Autowired
    private RentalService rentalService;

    @Autowired
    private DBUserService dbUserService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @GetMapping
    public ResponseEntity<?> getAllRentals(@RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");

        try {
            Jwt jwt = jwtDecoder.decode(jwtToken);
            String email = jwt.getClaim("sub");

            logger.debug("Get all rentals attempt by user with email: {}", email);
            List<Rental> rentals = rentalService.findAll();

            List<Map<String, Object>> rentalList = rentals.stream().map(rental -> {
                Map<String, Object> rentalMap = new HashMap<>();
                rentalMap.put("id", rental.getId());
                rentalMap.put("name", rental.getName());
                rentalMap.put("surface", rental.getSurface());
                rentalMap.put("price", rental.getPrice());
                rentalMap.put("picture", rental.getPicture() != null ? rental.getPicture() : null);
                rentalMap.put("description", rental.getDescription());
                rentalMap.put("owner_id", rental.getOwner().getId());
                rentalMap.put("created_at", rental.getCreatedAt());
                rentalMap.put("updated_at", rental.getUpdatedAt());
                return rentalMap;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("rentals", rentalList);

            return ResponseEntity.ok(response);
        } catch (JwtException e) {
            logger.debug("Invalid JWT token: {}", e.getMessage());
            return ResponseEntity.status(401).body("Invalid JWT token");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRentalById(@PathVariable Long id) {
        Rental rental = rentalService.findById(id);
        if (rental == null) {
            return ResponseEntity.status(404).body("Rental not found");
        }

        Map<String, Object> rentalMap = new HashMap<>();
        rentalMap.put("id", rental.getId());
        rentalMap.put("name", rental.getName());
        rentalMap.put("surface", rental.getSurface());
        rentalMap.put("price", rental.getPrice());
        rentalMap.put("picture", rental.getPicture() != null ? rental.getPicture() : null);
        rentalMap.put("description", rental.getDescription());
        rentalMap.put("owner_id", rental.getOwner().getId());
        rentalMap.put("created_at", rental.getCreatedAt());
        rentalMap.put("updated_at", rental.getUpdatedAt());

        return ResponseEntity.ok(rentalMap);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRental(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token,
            @RequestParam("name") String name,
            @RequestParam("surface") int surface,
            @RequestParam("price") int price,
            @RequestParam("description") String description,
            @RequestParam(value = "picture", required = false) MultipartFile picture) {
        String jwtToken = token.replace("Bearer ", "");

        try {
            Jwt jwt = jwtDecoder.decode(jwtToken);
            String email = jwt.getClaim("sub");

            logger.debug("Update rental attempt by user with email: {}", email);

            Rental rental = rentalService.findById(id);
            if (rental == null) {
                return ResponseEntity.status(404).body("Rental not found");
            }

            // Mettre à jour les champs du rental
            rental.setName(name);
            rental.setSurface((double) surface); // Convertir int en Double
            rental.setPrice((double) price); // Convertir int en Double
            rental.setDescription(description);

            // Mettre à jour l'image si nécessaire
            if (picture != null && !picture.isEmpty()) {
                String pictureFilename = System.currentTimeMillis() + "_" + picture.getOriginalFilename();
                Path picturePath = IMAGE_DIR.resolve(pictureFilename);
                Files.copy(picture.getInputStream(), picturePath);
                String pictureURL = "http://localhost:3001/api/rentals/images/" + pictureFilename;
                rental.setPicture(pictureURL);
            }

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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createRental(
            @RequestHeader("Authorization") String token,
            @RequestParam("name") String name,
            @RequestParam("surface") int surface,
            @RequestParam("price") int price,
            @RequestParam("description") String description,
            @RequestParam("picture") MultipartFile picture) {
        // Supprimer le préfixe "Bearer " du token
        String jwtToken = token.replace("Bearer ", "");

        try {
            // Décoder le token JWT pour extraire les claims
            Jwt jwt = jwtDecoder.decode(jwtToken);
            String email = jwt.getClaim("sub");

            logger.debug("Create rental attempt by user with email: {}", email);

            // Récupérer l'utilisateur à partir de l'email
            DBUser owner = dbUserService.findByEmail(email);
            if (owner == null) {
                return ResponseEntity.status(404).body("Owner not found");
            }

            // Créer un nouvel objet Rental
            Rental rental = new Rental();
            rental.setName(name);
            rental.setSurface((double) surface);
            rental.setPrice((double) price);
            rental.setDescription(description);
            rental.setOwner(owner);
            rental.setCreatedAt(new Timestamp(new java.util.Date().getTime()));
            rental.setUpdatedAt(new Timestamp(new java.util.Date().getTime()));

            // Enregistrer l'image si nécessaire
            if (!picture.isEmpty()) {
                String baseURL = "http://localhost:3001/api/rentals/images/";
                String pictureFilename = System.currentTimeMillis() + "_" + picture.getOriginalFilename();
                Path picturePath = Paths.get("uploads").resolve(pictureFilename);
                String pictureURL = baseURL + pictureFilename;

                // Ensure the directory exists
                if (!Files.exists(picturePath.getParent())) {
                    Files.createDirectories(picturePath.getParent());
                    logger.debug("Created directory: {}", picturePath.getParent().toAbsolutePath());
                }

                Files.copy(picture.getInputStream(), picturePath);
                logger.debug("Saved picture to: {}", picturePath.toAbsolutePath());
                rental.setPicture(pictureURL); // Set full URL for picture
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
}