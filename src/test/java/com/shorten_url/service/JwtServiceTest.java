package com.shorten_url.service;

import com.shorten_url.config.JwtConfig;
import com.shorten_url.entity.Jwt;
import com.shorten_url.entity.Role;
import com.shorten_url.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtServiceTest {

    @Mock
    private JwtConfig jwtConfig;

    @InjectMocks
    private JwtService jwtService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(Role.USER);

        when(jwtConfig.getAccessTokenExpiration()).thenReturn(3600);
        when(jwtConfig.getRefreshTokenExpiration()).thenReturn(86400);
        when(jwtConfig.getSecretKey()).thenReturn(mock(javax.crypto.SecretKey.class));
    }

    @Test
    void generateAccessToken_ShouldReturnJwt() {
        Jwt token = jwtService.generateAccessToken(user);

        assertNotNull(token);
    }

    @Test
    void generateRefreshToken_ShouldReturnJwt() {
        Jwt token = jwtService.generateRefreshToken(user);

        assertNotNull(token);
    }

}