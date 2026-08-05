package com.labortrack.security.Model.Dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequestDto(@Email @NotBlank String email,
                                  @NotBlank String password) {}
