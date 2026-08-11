package com.labortrack.security.Service.Auth;

import com.labortrack.security.Model.Dto.Request.AuthLoginRequestDto;
import com.labortrack.security.Model.Dto.Request.GoogleLoginRequestDto;
import com.labortrack.security.Model.Dto.Response.AuthLoginResponseDto;
import com.labortrack.security.Model.Entity.Usuario;
import com.labortrack.security.Repository.UsuarioRepository;
import com.labortrack.security.Utils.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UsuarioRepository usuarioRepository;
    @Value("${google.client.id}")
    private String googleClientId;

    public AuthService(UserDetailsService userDetailsService,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils,
                       UsuarioRepository usuarioRepository) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.usuarioRepository = usuarioRepository;
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
    // --- LOGIN CON GOOGLE OAUTH2 ---
    public AuthLoginResponseDto loginWithGoogle(GoogleLoginRequestDto requestDto) {
        try {
            // 1. Decodificar y validar firma del ID Token con Google
            JwtDecoder jwtDecoder = NimbusJwtDecoder
                    .withIssuerLocation("https://accounts.google.com")
                    .build();

            Jwt googleJwt = jwtDecoder.decode(requestDto.idToken());

            // 2. Verificar que el token pertenezca a nuestro Client ID
            if (!googleJwt.getAudience().contains(googleClientId)) {
                throw new BadCredentialsException("El ID Token no pertenece a esta aplicación");
            }

            String googleId = googleJwt.getSubject();
            String email = googleJwt.getClaimAsString("email");

            // 3. Buscar el usuario registrado. Si NO existe, se rechaza la autenticación
            Usuario usuario = usuarioRepository.findUserEntityByEmail(email)
                    .orElseThrow(() -> new BadCredentialsException("El usuario no se encuentra dado de alta en el sistema"));

            // 4. Si el usuario ya existe pero no tenía vinculado el googleId, lo asociamos en su primer login
            if (usuario.getGoogleId() == null) {
                usuario.setGoogleId(googleId);
                usuario = usuarioRepository.save(usuario);
            }

            // 5. Generar JWT propio con el rol real del usuario asignado en BD
            List<GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority(usuario.getRol().name())
            );

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    usuario.getEmail(),
                    null,
                    authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String accessToken = jwtUtils.createToken(authentication);

            return new AuthLoginResponseDto(email, "Login con Google OK", accessToken, true);

        } catch (BadCredentialsException e) {
            throw e; // Lanza el mensaje exacto de credencial/usuario inválido
        } catch (Exception e) {
            throw new BadCredentialsException("Token de Google inválido o expirado");
        }
    }

}
