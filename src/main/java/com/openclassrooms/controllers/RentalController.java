package com.openclassrooms.controllers;

import com.openclassrooms.model.DBUser;
import com.openclassrooms.model.Rental;
import com.openclassrooms.services.DBUserService;
import com.openclassrooms.services.RentalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    private static final Logger logger = LoggerFactory.getLogger(RentalController.class);

    @Autowired
    private RentalService rentalService;

    @Autowired
    private DBUserService dbUserService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @GetMapping
    public ResponseEntity<?> getAllRentals(@RequestHeader("Authorization") String token) {
        // Supprimer le préfixe "Bearer " du token
        String jwtToken = token.replace("Bearer ", "");

        try {
            // Décoder le token JWT pour extraire les claims
            Jwt jwt = jwtDecoder.decode(jwtToken);
            String email = jwt.getClaim("sub");

            logger.debug("Get all rentals attempt by user with email: {}", email);
            List<Rental> rentals = rentalService.findAll();
            return ResponseEntity.ok(rentals);
        } catch (JwtException e) {
            logger.debug("Invalid JWT token: {}", e.getMessage());
            return ResponseEntity.status(401).body("Invalid JWT token");
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
            rental.setSurface((double) surface); // Convertir int en Double
            rental.setPrice((double) price); // Convertir int en Double
            rental.setDescription(description);
            rental.setOwner(owner);

            // Vous pouvez également enregistrer l'image si nécessaire
            // byte[] pictureBytes = picture.getBytes();

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