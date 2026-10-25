package com.finance.auth_server.security;


import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.security.SecureRandom;


@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    RSAKey rsaKey(
            @Value("${security.jwt.private-key-base64}") String privateKeyBase64,
            @Value("${security.jwt.public-key-base64}") String publicKeyBase64) {

        try {
            // Decode the outer Base64 layer to get the complete PEM text.
            String privatePem = new String(
                    Base64.getDecoder().decode(privateKeyBase64),
                    StandardCharsets.UTF_8
            );

            String publicPem = new String(
                    Base64.getDecoder().decode(publicKeyBase64),
                    StandardCharsets.UTF_8
            );

            // Remove PEM headers, footers, and whitespace.
            String privateContent = privatePem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            String publicContent = publicPem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            // Decode the inner Base64 layer to get DER key bytes.
            byte[] privateBytes = Base64.getDecoder()
                    .decode(privateContent);

            byte[] publicBytes = Base64.getDecoder()
                    .decode(publicContent);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            RSAPrivateKey privateKey = (RSAPrivateKey)
                    keyFactory.generatePrivate(
                            new PKCS8EncodedKeySpec(privateBytes)
                    );

            RSAPublicKey publicKey = (RSAPublicKey)
                    keyFactory.generatePublic(
                            new X509EncodedKeySpec(publicBytes)
                    );

            // Ensure that the keys belong to the same RSA key pair.
            if (!java.util.Arrays.equals(
                    privateKey.getModulus().toByteArray(),
                    publicKey.getModulus().toByteArray())) {
                throw new IllegalArgumentException(
                        "RSA private and public keys do not match"
                );
            }

            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID("finance-auth-key-1")
                    .build();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to load RSA keys. Check their encoding and format.",
                    e
            );
        }
    }

    @Bean
    JwtEncoder jwtEncoder(RSAKey rsaKey) {
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
    }

    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }

    @Bean
    JwtDecoder jwtDecoder(RSAKey rsaKey) throws JOSEException {
        return NimbusJwtDecoder
                .withPublicKey(rsaKey.toRSAPublicKey())
                .build();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/register",
                                "/auth/login",
                                "/auth/refresh",
                                "/auth/forgot-password",
                                "/auth/reset-password",
                                "/actuator/health"
                        ).permitAll()
                        .requestMatchers("/auth/change-password", "/auth/logout", "/auth/revoke").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> {}));

        return http.build();
    }
}