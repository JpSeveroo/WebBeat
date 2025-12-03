package com.webbeat.webbeat.service;

import com.webbeat.webbeat.dto.UserDTO;
import com.webbeat.webbeat.model.PasswordResetToken;
import com.webbeat.webbeat.model.User;
import com.webbeat.webbeat.repository.PasswordResetRepository;
import com.webbeat.webbeat.repository.UserRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetRepository passwordResetRepository;
    private final JavaMailSender javaMailSender;
    private final Map<String, Long> rateLimitMap = new ConcurrentHashMap<>();
    private static final long RATE_LIMIT_DURATION = 24 * 60 * 60 * 1000L;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, PasswordResetRepository passwordResetRepository, JavaMailSender javaMailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetRepository = passwordResetRepository;
        this.javaMailSender = javaMailSender;
    }

    public User registerNewUser(UserDTO request) {

        userRepository.findByEmail(request.email()).ifPresent(user -> {
            throw new IllegalStateException("Email already in use");
        });

        String passwordHash = passwordEncoder.encode(request.password());

        User user = new User (
                null,
                request.email(),
                passwordHash
        );

        return userRepository.save(user);
    }

    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        long lastRequestTime = rateLimitMap.getOrDefault(email, 0L);

        if (currentTime - lastRequestTime < RATE_LIMIT_DURATION) {
            return;
        }
        rateLimitMap.put(email, currentTime);

        String token = UUID.randomUUID().toString();
        Instant expiry = Instant.now().plus(15, ChronoUnit.MINUTES);

        PasswordResetToken passwordResetToken = new PasswordResetToken(
                null,
                token,
                user.id(),
                expiry
        );

        passwordResetRepository.save(passwordResetToken);

        sendResetEmail(user.email(), token);
    }

    public void completeResetPassword(String token, String newPassword) {

        PasswordResetToken resetToken = passwordResetRepository.findByToken(token);

        if (resetToken == null) {
            throw new IllegalStateException("Invalid password reset token.");
        }

        if (resetToken.expireDate().isBefore(Instant.now())) {
            throw new IllegalStateException("Password reset timer has expired. Please request a new one on the login page.");
        }

        User user = userRepository.findById(resetToken.userId())
                .orElseThrow(() -> new IllegalStateException("User not found."));

        User updatedUser = new User(
                user.id(),
                user.email(),
                passwordEncoder.encode(newPassword)
        );
        userRepository.save(updatedUser);

        passwordResetRepository.delete(resetToken);
    }

    private void sendResetEmail(String email, String token) {
        String resetUrl = "http://localhost:8080/auth/reset-password?token=" + token;
        
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #111827; color: #ffffff; border-radius: 10px;">
                <h2 style="color: #a78bfa; text-align: center;">WebBeat Security</h2>
                <p style="font-size: 16px; color: #d1d5db; text-align: center;">
                    You have requested to reset your password. Click the button below to proceed.
                </p>
                <div style="text-align: center; margin: 30px 0;">
                    <a href="%s" style="background-color: #7c3aed; color: #ffffff; padding: 14px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; font-size: 16px; display: inline-block;">
                        Reset Password
                    </a>
                </div>
                <p style="font-size: 14px; color: #9ca3af; text-align: center;">
                    This link expires in 15 minutes. If you did not request this, please ignore this email.
                </p>
                <hr style="border: 0; border-top: 1px solid #374151; margin: 20px 0;">
                <p style="font-size: 12px; color: #6b7280; text-align: center;">
                    WebBeat Monitoring Systems
                </p>
            </div>
            """.formatted(resetUrl);

        try {
            jakarta.mail.internet.MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(mimeMessage, "utf-8");

            helper.setText(htmlContent, true); // true = Enable HTML
            helper.setTo(email);
            helper.setSubject("WebBeat - Reset Your Password");
            helper.setFrom("webbeat.suporte@gmail.com");

            javaMailSender.send(mimeMessage);

        } catch (jakarta.mail.MessagingException e) {
            throw new IllegalStateException("Failed to send email", e);
        }
    }
}















