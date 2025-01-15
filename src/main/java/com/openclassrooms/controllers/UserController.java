package com.openclassrooms.controllers;

import com.openclassrooms.dto.DBUserDTO;
import com.openclassrooms.services.DBUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private DBUserService dbUserService;

    @Operation(summary = "Get a user by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DBUserDTO> findById(@PathVariable Integer id) {
        DBUserDTO userDTO = dbUserService.getUserDTOById(id);
        return ResponseEntity.ok(userDTO);
    }
}