package com.finance.auth_server.security;

import com.finance.auth_server.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class JwtTokenService {

    private final JwtEncoder encoder;
    private final long accessTokenSeconds;
    private final String issuer;

    public JwtTokenService(
            JwtEncoder encoder,
            @Value("${security.jwt.access-token-seconds:900}") long accessTokenSeconds,
            @Value("${security.jwt.issuer:finance-auth-service}") String issuer) {
        this.encoder = encoder;
        this.accessTokenSeconds = accessTokenSeconds;
        this.issuer = issuer;
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(accessTokenSeconds))
                .id(UUID.randomUUID().toString())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("scope", List.of("USER"))
                .build();

        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public long getAccessTokenSeconds() {
        return accessTokenSeconds;
    }
}
