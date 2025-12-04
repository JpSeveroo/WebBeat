package com.webbeat.webbeat.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserDTO(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format") // Bonus: Checks for @ symbol
        String email,

        @NotBlank(message = "Password is required")
        String password
) {}
