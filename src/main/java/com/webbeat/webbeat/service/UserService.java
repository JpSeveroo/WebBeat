package com.webbeat.webbeat.service;

import com.webbeat.webbeat.dto.RegisterRequest;
import com.webbeat.webbeat.model.User;
import com.webbeat.webbeat.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerNewUser(RegisterRequest request) {

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
}
