package com.labortrack.security.Model.Dto.Request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDto(
        @NotBlank(message = "El token es obligatorio")
        String token,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres")
        String newPassword
) {}