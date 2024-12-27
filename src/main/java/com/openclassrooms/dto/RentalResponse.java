package com.openclassrooms.dto;

import java.util.List;

public class RentalResponse {
    private List<RentalDTO> rentals;

    public List<RentalDTO> getRentals() {
        return rentals;
    }

    public void setRentals(List<RentalDTO> rentals) {
        this.rentals = rentals;
    }
}