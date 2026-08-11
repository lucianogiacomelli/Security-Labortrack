package com.labortrack.security.Model.Dto.Response;

import com.labortrack.security.Model.Entity.RefreshToken;

public record RefreshTokenResponse(String rawToken, RefreshToken refreshToken) {
}
