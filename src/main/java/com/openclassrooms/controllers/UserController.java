package com.openclassrooms.controllers;

import com.openclassrooms.model.DBUser;
import com.openclassrooms.services.DBUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private DBUserService dbUserService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        DBUser user = dbUserService.findById(id);
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }
        return ResponseEntity.ok(user);
    }
}