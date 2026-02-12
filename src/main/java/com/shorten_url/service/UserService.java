package com.shorten_url.service;

import com.shorten_url.dto.RegisterRequest;
import com.shorten_url.entity.Role;
import com.shorten_url.entity.User;
import com.shorten_url.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder encoder;

    private final UserRepository userRepository;


    @Transactional
    public User register(RegisterRequest registerRequest) {
        if (registerRequest.getEmail() == null || registerRequest.getPassword() == null) {
            throw new IllegalArgumentException("Email and password must not be null");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(encoder.encode(registerRequest.getPassword()));
        user.setRole(Role.USER);
        return userRepository.save(user);
    }
}
