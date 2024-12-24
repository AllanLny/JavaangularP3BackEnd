package com.openclassrooms.dto;

import java.sql.Timestamp;

public class MessageDTO {
    private Long id;
    private String message;
    private Long user_id;
    private Long rental_id;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Getters
    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public Long getUser_id() {
        return user_id;
    }

    public Long getRental_id() {
        return rental_id;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public void setRental_id(Long rental_id) {
        this.rental_id = rental_id;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}