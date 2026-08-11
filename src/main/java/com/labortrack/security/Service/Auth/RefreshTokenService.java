package com.labortrack.security.Service.Auth;

import com.labortrack.security.Model.Dto.Response.RefreshTokenResponse;
import com.labortrack.security.Model.Entity.RefreshToken;
import com.labortrack.security.Model.Entity.Usuario;
import com.labortrack.security.Repository.RefreshTokenRepository;
import com.labortrack.security.Repository.UsuarioRepository;
import com.labortrack.security.Utils.HashUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${jwt.refresh.expiration-ms:604800000}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioRepository usuarioRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UsuarioRepository usuarioRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.usuarioRepository = usuarioRepository;
    }


    // Hashea el token recibido antes de consultar en PostgreSQL
    public Optional<RefreshToken> findByToken(String rawToken) {
        String hashedToken = HashUtils.hashToken(rawToken);
        return refreshTokenRepository.findByToken(hashedToken);
    }

    @Transactional
    public RefreshTokenResponse createRefreshToken(String email) {
        Usuario usuario = usuarioRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));

        RefreshToken refreshToken = refreshTokenRepository.findByUsuario(usuario)
                .orElseGet(RefreshToken::new);

        String rawToken = UUID.randomUUID().toString();
        String hashedToken = HashUtils.hashToken(rawToken);

        refreshToken.setUsuario(usuario);
        refreshToken.setToken(hashedToken); // Persiste únicamente el hash
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);

        return new RefreshTokenResponse(rawToken, savedToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("El Refresh Token expiró. Por favor inicie sesión nuevamente.");
        }
        return token;
    }

    // Hashea el token recibido antes de ejecutar el DELETE
    @Transactional
    public void deleteByToken(String rawToken) {
        String hashedToken = HashUtils.hashToken(rawToken);
        refreshTokenRepository.deleteByToken(hashedToken);
    }
}