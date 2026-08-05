package com.labortrack.security.Model.Dto.Request;

import com.labortrack.security.Model.Entity.RolNombre;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUsuarioRequestDto(
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String nombre,
        @NotBlank String apellido,
        @NotNull RolNombre rol
        ) {
}
