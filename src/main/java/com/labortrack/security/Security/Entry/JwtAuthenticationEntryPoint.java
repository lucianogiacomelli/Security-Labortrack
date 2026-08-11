package com.labortrack.security.Security.Entry;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        String errorType = (String) request.getAttribute("jwt_error");

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // HTTP 401
        response.setContentType("application/json;charset=UTF-8");

        if ("TOKEN_EXPIRED".equals(errorType)) {
            response.getWriter().write("""
                {
                    "code": "TOKEN_EXPIRED",
                    "message": "El accessToken ha expirado. Solicite renovación con el refreshToken."
                }
            """);
        } else {
            response.getWriter().write("""
                {
                    "code": "TOKEN_INVALID",
                    "message": "El token es inválido o no existe."
                }
            """);
        }
    }
}