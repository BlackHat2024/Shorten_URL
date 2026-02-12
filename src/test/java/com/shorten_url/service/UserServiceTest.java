package com.shorten_url.service;

import com.shorten_url.dto.RegisterRequest;
import com.shorten_url.entity.Role;
import com.shorten_url.entity.User;
import com.shorten_url.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock PasswordEncoder encoder;
    @Mock UserRepository userRepository;

    @InjectMocks UserService userService;

    @Test
    void register_success_encodesPassword_setsRole_saves() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@test.com");
        req.setPassword("pass1234");

        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(encoder.encode("pass1234")).thenReturn("ENC");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.register(req);

        assertThat(saved.getEmail()).isEqualTo("test@test.com");
        assertThat(saved.getPassword()).isEqualTo("ENC");
        assertThat(saved.getRole()).isEqualTo(Role.USER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_throws_whenEmailOrPasswordNull() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail(null);
        req.setPassword(null);

        assertThatThrownBy(() -> userService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be null");

        verifyNoInteractions(userRepository);
    }

    @Test
    void register_throws_whenEmailExists() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@test.com");
        req.setPassword("pass1234");

        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any());
    }
}
