package com.openclassrooms.repository;

import com.openclassrooms.model.DBUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DBUserRepository extends JpaRepository<DBUser, Integer> {
    DBUser findByEmail(String email);
}