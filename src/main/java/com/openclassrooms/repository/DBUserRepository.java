package com.openclassrooms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.openclassrooms.model.DBUser;

public interface DBUserRepository extends JpaRepository<DBUser, Integer> {
    public DBUser findByUsername(String username);
}

