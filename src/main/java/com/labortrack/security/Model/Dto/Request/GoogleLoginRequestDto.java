package com.labortrack.security.Model.Dto.Request;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequestDto(
        @NotBlank String idToken
) {}
