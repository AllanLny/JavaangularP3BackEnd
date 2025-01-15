package com.openclassrooms.controllers;

import com.openclassrooms.dto.MessageResponseDTO;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {

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
    public ResponseEntity<MessageResponseDTO> createRental(
            @RequestHeader("Authorization") String token,
            @RequestParam("name") String name,
            @RequestParam("surface") int surface,
            @RequestParam("price") int price,
            @RequestParam("description") String description,
            @RequestParam("picture") MultipartFile picture) throws Exception {
        String jwtToken = token.replace("Bearer ", "");
        Jwt jwt = jwtDecoder.decode(jwtToken);
        String email = jwt.getClaim("sub");

        DBUser owner = dbUserService.findByEmail(email);

        rentalService.saveRental(name, surface, price, description, picture, owner);

        return ResponseEntity.ok(new MessageResponseDTO("Rental created successfully"));
    }
    @GetMapping
    @Operation(summary = "View a list of available rentals")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "401", description = "Invalid JWT token")
    })
    public ResponseEntity<RentalResponse> getAllRentals() {
        List<RentalDTO> rentals = rentalService.findAll();
        return ResponseEntity.ok(new RentalResponse(rentals));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a rental by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved rental"),
            @ApiResponse(responseCode = "404", description = "Rental not found")
    })
    public ResponseEntity<RentalDTO> getRentalById(@PathVariable Long id) {
        RentalDTO rentalDTO = rentalService.findById(id);
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
            @RequestHeader("Authorization") String token) throws Exception {
        RentalDTO updatedRentalDTO = rentalService.updateRental(id, name, surface, price, description);
        return ResponseEntity.ok(updatedRentalDTO);
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
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}