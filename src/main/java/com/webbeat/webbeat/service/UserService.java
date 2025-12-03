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
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetRepository passwordResetRepository;
    private final JavaMailSender javaMailSender;

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

    private void sendResetEmail(String email, String token) {
        String resetUrl = "localhost:8080/auth/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("WebBeat - Password Reset Request");
        message.setText("Click the link below to reset your password:\n\n" + resetUrl + "\n\nLink expires in 15 minutes.");

        javaMailSender.send(message);
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
}















