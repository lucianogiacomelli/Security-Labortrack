package com.labortrack.security.Model.Dto.Response;

import com.labortrack.security.Model.Entity.RolNombre;

public record UserResponseDto (String nombre, String apellido, String email, RolNombre rol)
{}
