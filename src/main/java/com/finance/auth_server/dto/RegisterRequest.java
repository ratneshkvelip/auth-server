package com.finance.auth_server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 20) String mobileNumber,
        @NotBlank @Size(min = 8, max = 100) String password
) { }
