package com.shorten_url.service;

import com.shorten_url.entity.User;
import com.shorten_url.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock UserRepository userRepository;
    @InjectMocks CustomUserDetailsService service;

    @Test
    void loadUserByUsername_returnsUserDetails_whenFound() {
        User u = new User();
        u.setEmail("a@test.com");
        u.setPassword("ENC");

        when(userRepository.findByEmail("a@test.com")).thenReturn(Optional.of(u));

        UserDetails details = service.loadUserByUsername("a@test.com");

        assertThat(details.getUsername()).isEqualTo("a@test.com");
        assertThat(details.getPassword()).isEqualTo("ENC");
        assertThat(details.getAuthorities()).extracting("authority").contains("ROLE_USER");
    }

    @Test
    void loadUserByUsername_throws_whenNotFound() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing@test.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}
