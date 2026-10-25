package com.finance.auth_server.service;

import com.finance.auth_server.dto.MessageResponse;
import com.finance.auth_server.dto.RegisterRequest;
import com.finance.auth_server.entity.User;
import com.finance.auth_server.exception.ConflictException;
import com.finance.auth_server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
            ) {
        this.userRepository = userRepository;
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


}
