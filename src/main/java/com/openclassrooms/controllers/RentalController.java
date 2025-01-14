package com.openclassrooms.controllers;

import com.openclassrooms.dto.RentalDTO;
import com.openclassrooms.dto.RentalResponse;
import com.openclassrooms.model.DBUser;
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
import java.nio.file.Path;
import java.sql.Timestamp;
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

            rentalService.saveRental(name, surface, price, description, picture, owner);

            RentalResponse response = new RentalResponse();
            response.setMessage("Rental created !");

            return ResponseEntity.ok(response);
        } catch (JwtException e) {
            return ResponseEntity.status(401).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping
    @Operation(summary = "View a list of available rentals")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "401", description = "Invalid JWT token")
    })
    public ResponseEntity<RentalResponse> getAllRentals(){
            List<RentalDTO> rentals = rentalService.findAll();
            RentalResponse response = new RentalResponse();
            response.setRentals(rentals);
            return ResponseEntity.ok(response);
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
            RentalDTO updatedRentalDTO = rentalService.updateRental(id, name, surface, price, description);
            if (updatedRentalDTO == null) {
                return ResponseEntity.status(404).body(null);
            }

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
        Path file = RentalService.IMAGE_DIR.resolve(filename);
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