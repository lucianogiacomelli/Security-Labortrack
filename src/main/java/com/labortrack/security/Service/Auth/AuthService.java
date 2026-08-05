package com.labortrack.security.Service.Auth;

import com.labortrack.security.Model.Dto.Request.AuthLoginRequestDto;
import com.labortrack.security.Model.Dto.Response.AuthLoginResponseDto;
import com.labortrack.security.Utils.JwtUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(UserDetailsService userDetailsService,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public AuthLoginResponseDto loginUser(AuthLoginRequestDto authLoginRequestDto){
        String email = authLoginRequestDto.email();
        String password = authLoginRequestDto.password();
        Authentication authentication = this.authenticate (email, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = jwtUtils.createToken(authentication);

        AuthLoginResponseDto authLoginResponseDto = new AuthLoginResponseDto(email, "Login OK", accessToken, true );
        return authLoginResponseDto;
    }

    public Authentication authenticate(String email, String password) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // Protege cuentas creadas por Google (password="") y contraseñas vacías
        if (userDetails.getPassword().isEmpty() || password == null || password.isBlank() ||
                !passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }

        return new UsernamePasswordAuthenticationToken(email, userDetails.getPassword(), userDetails.getAuthorities());
    }
}
