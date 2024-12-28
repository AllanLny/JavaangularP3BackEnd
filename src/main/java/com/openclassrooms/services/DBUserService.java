package com.openclassrooms.services;

import com.openclassrooms.model.DBUser;
import com.openclassrooms.repository.DBUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.sql.Timestamp;

@Service
public class DBUserService {

    private static final Logger logger = LoggerFactory.getLogger(DBUserService.class);

    @Autowired
    private DBUserRepository dbUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtEncoder jwtEncoder;

    public DBUser authenticate(String email, String password) {
        DBUser user = dbUserRepository.findByEmail(email);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    public String generateToken(DBUser user) {
        try {
            Instant now = Instant.now();
            long expiry = 3600L; // 1 hour

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

    public DBUser findById(Integer id) {
        return dbUserRepository.findById(id).orElse(null);
    }

    public DBUser findByEmail(String email) {
        return dbUserRepository.findByEmail(email);
    }

    public String registerUser(DBUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        user.setCreatedAt(currentTimestamp);
        user.setUpdatedAt(currentTimestamp);

        dbUserRepository.save(user);
        return generateToken(user);
    }
}