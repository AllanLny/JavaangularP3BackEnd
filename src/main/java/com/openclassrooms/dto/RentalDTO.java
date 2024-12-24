package com.openclassrooms.dto;

import java.sql.Timestamp;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public class RentalDTO {
    private Long id;
    private String name;
    private Double surface;
    private Double price;
    private List<String> picture;
    private String description;
    private Long owner_id;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private MultipartFile pictureFile; // Ajout du champ MultipartFile pour l'image

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getSurface() {
        return surface;
    }

    public Double getPrice() {
        return price;
    }

    public List<String> getPicture() {
        return picture;
    }

    public String getDescription() {
        return description;
    }



    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public MultipartFile getPictureFile() {
        return pictureFile;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurface(Double surface) {
        this.surface = surface;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setPicture(List<String> picture) {
        this.picture = picture;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(Long owner_id) {
        this.owner_id = owner_id;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setPictureFile(MultipartFile pictureFile) {
        this.pictureFile = pictureFile;
    }
}