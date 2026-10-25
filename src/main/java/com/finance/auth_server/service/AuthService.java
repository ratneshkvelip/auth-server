package com.finance.auth_server.service;

import com.finance.auth_server.dto.LoginRequest;
import com.finance.auth_server.dto.MessageResponse;
import com.finance.auth_server.dto.RegisterRequest;
import com.finance.auth_server.dto.TokenResponse;
import com.finance.auth_server.entity.RefreshToken;
import com.finance.auth_server.entity.User;
import com.finance.auth_server.enums.UserStatus;
import com.finance.auth_server.exception.ConflictException;
import com.finance.auth_server.exception.UnauthorizedException;
import com.finance.auth_server.repository.RefreshTokenRepository;
import com.finance.auth_server.repository.UserRepository;
import com.finance.auth_server.security.JwtTokenService;
import com.finance.auth_server.security.TokenGenerator;
import com.finance.auth_server.security.TokenHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
public class AuthService {


    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);
    private static final Duration RESET_TOKEN_TTL = Duration.ofMinutes(15);


    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final TokenGenerator tokenGenerator;
    private final TokenHasher tokenHasher;

    @Autowired
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RefreshTokenRepository refreshTokenRepository,
            JwtTokenService jwtTokenService,
            TokenHasher tokenHasher,
            TokenGenerator tokenGenerator
            ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenService = jwtTokenService;
        this.tokenGenerator= tokenGenerator;
        this.tokenHasher=tokenHasher;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Email is already registered");
        }
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new ConflictException("Username is already registered");
        }
        if (request.mobileNumber() != null && !request.mobileNumber().isBlank()
                && userRepository.existsByMobileNumber(request.mobileNumber())) {
            throw new ConflictException("Mobile number is already registered");
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        userRepository.save(new User(
                request.username().trim(),
                request.email().trim().toLowerCase(),
                request.name().trim(),
                request.mobileNumber(),
                encodedPassword
        ));

        return new MessageResponse("User registered successfully");
    }


    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("User account is not active");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        return issueTokens(user);

    }
    private TokenResponse issueTokens(User user) {
        String accessToken = jwtTokenService.createAccessToken(user);
        String refreshToken = tokenGenerator.generate();

        refreshTokenRepository.save(new RefreshToken(
                user.getId(),
                tokenHasher.hash(refreshToken),
                Instant.now().plus(REFRESH_TOKEN_TTL)
        ));

        return new TokenResponse(
                "Bearer",
                accessToken,
                refreshToken,
                jwtTokenService.getAccessTokenSeconds()
        );
    }

}
