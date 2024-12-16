package com.openclassrooms.services;

import com.openclassrooms.model.DBUser;
import com.openclassrooms.repository.DBUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class DBUserService {
    private static final Logger logger = LoggerFactory.getLogger(DBUserService.class);

    @Autowired
    private DBUserRepository dbUserRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private JwtEncoder jwtEncoder;

    public String registerUser(DBUser user) {
        logger.debug("Registering user with email: {}", user.getEmail());
        if (dbUserRepository.findByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException("Email already in use");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(Timestamp.from(Instant.now()));
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        dbUserRepository.save(user);
        logger.debug("User registered successfully: {}", user.getEmail());
        return generateToken(user);
    }

    public String generateToken(DBUser user) {
        try {
            logger.debug("Generating token for user: {}", user.getEmail());
            Instant now = Instant.now();
            long expiry = 36000L; // 10 heures

            Map<String, Object> userClaims = new HashMap<>();
            userClaims.put("sub", user.getEmail());
            userClaims.put("name", user.getName());
            userClaims.put("iat", now.getEpochSecond());
            userClaims.put("exp", now.plusSeconds(expiry).getEpochSecond());

            JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                    .claims(claims -> claims.putAll(userClaims))
                    .build();

            JwsHeader jwsHeader = JwsHeader.with(() -> "HS256").build();
            JwtEncoderParameters jwtEncoderParameters = JwtEncoderParameters.from(jwsHeader, claimsSet);
            String token = jwtEncoder.encode(jwtEncoderParameters).getTokenValue();
            logger.debug("Token generated successfully for user: {}", user.getEmail());
            return token;
        } catch (Exception e) {
            logger.error("Error generating token for user: {}", user.getEmail(), e);
            throw new RuntimeException("Error generating token", e);
        }
    }

    public DBUser findByEmail(String email) {
        logger.debug("Finding user by email: {}", email);
        DBUser user = dbUserRepository.findByEmail(email);
        if (user == null) {
            logger.debug("User not found with email: {}", email);
        } else {
            logger.debug("User found with email: {}", email);
        }
        return user;
    }

    public DBUser findById(Integer id) {
        return dbUserRepository.findById(id).orElse(null);
    }
}