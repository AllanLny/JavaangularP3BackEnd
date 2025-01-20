package com.openclassrooms.services;

import com.openclassrooms.dto.AuthResponseDTO;
import com.openclassrooms.dto.DBUserDTO;
import com.openclassrooms.dto.RegisterUserDTO;
import com.openclassrooms.dto.TokenResponseDTO;
import com.openclassrooms.model.DBUser;
import com.openclassrooms.repository.DBUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class DBUserService {

    private final DBUserRepository dbUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    @Value("${jwt.secret}")
    private String jwtKey;

    @Autowired
    public DBUserService(DBUserRepository dbUserRepository, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder) {
        this.dbUserRepository = dbUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    public DBUser authenticate(String email, String password) {
        DBUser user = dbUserRepository.findByEmail(email);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        throw new AccessDeniedException("Invalid email or password");
    }

    public String generateToken(DBUser user) {
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
        return jwtEncoder.encode(jwtEncoderParameters).getTokenValue();
    }

    public DBUser findById(Integer id) {
        Optional<DBUser> user = dbUserRepository.findById(id);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        return user.get();
    }

    public DBUser findByEmail(String email) {
        DBUser user = dbUserRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        return user;
    }

    public TokenResponseDTO registerUser(RegisterUserDTO registerUser) {
        try {
            DBUser user = new DBUser();
            user.setEmail(registerUser.getEmail());
            user.setName(registerUser.getName());

            // Ensure the password is set before encoding it
            if (registerUser.getPassword() == null) {
                throw new IllegalArgumentException("Password cannot be null");
            }
            user.setPassword(passwordEncoder.encode(registerUser.getPassword()));

            Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
            user.setCreatedAt(currentTimestamp);
            user.setUpdatedAt(currentTimestamp);

            dbUserRepository.save(user);
            String token = this.generateToken(user);
            return new TokenResponseDTO(token);
        } catch (Exception e) {
            throw new RuntimeException("Error registering user: " + e.getMessage());
        }
    }

    public TokenResponseDTO login(String email, String password) {
        DBUser user = authenticate(email, password);
        String token = generateToken(user);
        return new TokenResponseDTO(token);
    }

    public DBUserDTO getUserDTOById(Integer id) {
        DBUser user = findById(id);
        return convertToDTO(user);
    }
    
    public DBUserDTO convertToDTO(DBUser user) {
        DBUserDTO userDTO = new DBUserDTO();
        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setName(user.getName());
        userDTO.setCreatedAt(user.getCreatedAt().toString());
        userDTO.setUpdatedAt(user.getUpdatedAt().toString());
        return userDTO;
    }
}