package com.labortrack.security.Controller;

import com.labortrack.security.Model.Dto.Request.AuthLoginRequestDto;
import com.labortrack.security.Model.Dto.Response.AuthLoginResponseDto;
import com.labortrack.security.Service.Auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthService authService;

    public AuthenticationController (AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthLoginResponseDto> loginUser (@Valid @RequestBody AuthLoginRequestDto authLoginRequestDto){
        AuthLoginResponseDto authLoginResponseDto = authService.loginUser(authLoginRequestDto);
        return ResponseEntity.ok(authLoginResponseDto);
    }
}
