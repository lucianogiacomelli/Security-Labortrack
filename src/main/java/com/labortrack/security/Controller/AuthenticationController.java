package com.labortrack.security.Controller;

import com.labortrack.security.Model.Dto.Request.AuthLoginRequestDto;
import com.labortrack.security.Model.Dto.Request.GoogleLoginRequestDto;
import com.labortrack.security.Model.Dto.Request.RefreshTokenRequestDto;
import com.labortrack.security.Model.Dto.Response.AuthLoginResponseDto;
import com.labortrack.security.Model.Entity.RefreshToken;
import com.labortrack.security.Service.Auth.AuthService;
import com.labortrack.security.Service.Auth.RefreshTokenService;
import com.labortrack.security.Utils.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtils jwtUtils;

    public AuthenticationController (AuthService authService,
                                     RefreshTokenService refreshTokenService,
                                     JwtUtils jwtUtils) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthLoginResponseDto> loginUser (@Valid @RequestBody AuthLoginRequestDto authLoginRequestDto){
        AuthLoginResponseDto authLoginResponseDto = authService.loginUser(authLoginRequestDto);
        return ResponseEntity.ok(authLoginResponseDto);
    }

    @PostMapping("/google")
    public ResponseEntity<AuthLoginResponseDto> loginWithGoogle(@Valid @RequestBody GoogleLoginRequestDto authGoogleLoginRequestDto) {
        AuthLoginResponseDto response = authService.loginWithGoogle(authGoogleLoginRequestDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthLoginResponseDto> refreshToken(@RequestBody @Valid RefreshTokenRequestDto request) {
        return refreshTokenService.findByToken(request.refreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUsuario)
                .map(usuario -> {
                    // 1. Mapear el rol del usuario a GrantedAuthority
                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority(usuario.getRol().name())
                    );

                    // 2. Crear la autenticación incluyendo las autoridades
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            usuario.getEmail(),
                            null,
                            authorities
                    );

                    // 3. Generar el token
                    String newAccessToken = jwtUtils.createToken(authentication);

                    return ResponseEntity.ok(new AuthLoginResponseDto(
                            usuario.getEmail(),
                            "Token renovado con éxito",
                            newAccessToken,
                            request.refreshToken(),
                            true
                    ));
                })
                .orElseThrow(() -> new RuntimeException("Refresh Token inválido o no existente"));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody @Valid RefreshTokenRequestDto request) {
        refreshTokenService.deleteByToken(request.refreshToken());
        return ResponseEntity.ok("Sesión cerrada exitosamente");
    }

}
