package com.labortrack.security.Service.Auth;

import com.labortrack.security.Model.Dto.Request.AuthLoginRequestDto;
import com.labortrack.security.Model.Dto.Request.GoogleLoginRequestDto;
import com.labortrack.security.Model.Dto.Response.AuthLoginResponseDto;
import com.labortrack.security.Model.Dto.Response.RefreshTokenResponse;
import com.labortrack.security.Model.Entity.PasswordResetToken;
import com.labortrack.security.Model.Entity.RefreshToken;
import com.labortrack.security.Model.Entity.Usuario;
import com.labortrack.security.Repository.PasswordResetTokenRepository;
import com.labortrack.security.Repository.UsuarioRepository;
import com.labortrack.security.Service.EmailService;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Value("${google.client.id}")
    private String googleClientId;

    public AuthService(UserDetailsService userDetailsService,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils,
                       UsuarioRepository usuarioRepository,
                       RefreshTokenService refreshTokenService,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       EmailService emailService) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenService = refreshTokenService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
    }

    public AuthLoginResponseDto loginUser(AuthLoginRequestDto authLoginRequestDto){
        String email = authLoginRequestDto.email();
        String password = authLoginRequestDto.password();
        Authentication authentication = this.authenticate (email, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String accessToken = jwtUtils.createToken(authentication);

        RefreshTokenResponse refreshToken = refreshTokenService.createRefreshToken(email);
        AuthLoginResponseDto authLoginResponseDto = new AuthLoginResponseDto(email, "Login OK", accessToken, refreshToken.rawToken(),true );
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
            RefreshTokenResponse refreshToken = refreshTokenService.createRefreshToken(email);

            return new AuthLoginResponseDto(email, "Login con Google OK", accessToken,refreshToken.rawToken(), true);

        } catch (BadCredentialsException e) {
            throw e; // Lanza el mensaje exacto de credencial/usuario inválido
        } catch (Exception e) {
            throw new BadCredentialsException("Token de Google inválido o expirado");
        }
    }
    // --- RECUPERO DE CONTRASEÑA ---

    @Transactional
    public void processForgotPassword(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findUserEntityByEmail(email);

        // Si no existe, retorna sin error para prevenir la enumeración de usuarios
        if (usuarioOpt.isEmpty()) {
            return;
        }

        Usuario usuario = usuarioOpt.get();

        // Borra tokens anteriores del usuario si existían
        passwordResetTokenRepository.deleteByUsuario(usuario);

        // Genera el token único con 15 minutos de validez
        String rawToken = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUsuario(usuario);
        resetToken.setToken(rawToken);
        resetToken.setExpiryDate(Instant.now().plus(15, ChronoUnit.MINUTES));

        passwordResetTokenRepository.save(resetToken);

        // Envía el correo SMTP
        emailService.sendPasswordResetEmail(usuario.getEmail(), rawToken);
    }

    @Transactional
    public void processResetPassword(String rawToken, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(rawToken)
                .orElseThrow(() -> new RuntimeException("El enlace de recuperación es inválido o no existe."));

        if (Instant.now().isAfter(resetToken.getExpiryDate())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new RuntimeException("El enlace de recuperación ha expirado. Solicite uno nuevo.");
        }

        Usuario usuario = resetToken.getUsuario();

        // Actualiza contraseña encriptada
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);

        // Limpieza: revoca el token usado y liquida los refresh tokens activos por seguridad
        passwordResetTokenRepository.delete(resetToken);
        refreshTokenService.deleteByUsuario(usuario);
    }

}
