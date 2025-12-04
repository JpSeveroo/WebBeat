package com.webbeat.webbeat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordDTO(

        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String newPassword,

        @NotBlank(message = "Confirmation is required")
        String confirmPassword
) {
    public boolean isConfirmed() {
        assert newPassword != null;
        return newPassword.equals(confirmPassword);
    }
}
