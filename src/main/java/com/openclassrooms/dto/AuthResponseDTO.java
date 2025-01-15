package com.openclassrooms.dto;

public class AuthResponseDTO {
    private String token;
    private DBUserDTO user;

    public AuthResponseDTO(String token, DBUserDTO user) {
        this.token = token;
        this.user = user;
    }

    // Getters and setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public DBUserDTO getUser() {
        return user;
    }

    public void setUser(DBUserDTO user) {
        this.user = user;
    }
}