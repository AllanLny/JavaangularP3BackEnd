package com.openclassrooms.services;

import com.openclassrooms.model.DBUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class JWTService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${jwt.secret}")
    private String jwtKey;

    public JWTService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
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

    public Map<String, Object> decodeToken(String token) {
        String jwtToken = token.replace("Bearer ", "");
        Jwt jwt = jwtDecoder.decode(jwtToken);
        return jwt.getClaims();
    }
}