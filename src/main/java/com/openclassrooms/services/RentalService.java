package com.openclassrooms.services;

import com.openclassrooms.model.Rental;
import com.openclassrooms.repository.RentalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RentalService {

    @Autowired
    private RentalRepository rentalRepository;

    public List<Rental> findAll() {
        return rentalRepository.findAll();
    }

    public void save(Rental rental) {
        rentalRepository.save(rental);
    }

    public Rental findById(Long id) {
        return rentalRepository.findById(id).orElse(null);
    }
    
}
