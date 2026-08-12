package com.labortrack.security.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String username;

    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username}") String username) {
        this.mailSender = mailSender;
        this.username = username;
    }

    public void sendPasswordResetEmail(String to, String resetToken) {
        String resetUrl = "http://localhost:5173/reset-password?token=" + resetToken; // URL del Front (React/Angular)

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(username);
        message.setTo(to);
        message.setSubject("LaborTrack - Recuperación de Contraseña");
        message.setText("Hola,\n\nSolicitaste restablecer tu contraseña. Hacé clic en el siguiente enlace para continuar:\n\n"
                + resetUrl + "\n\nEste enlace expira en 15 minutos.\nSi no solicitaste este cambio, ignorá este correo.");

        mailSender.send(message);
    }
}
