package com.shorten_url.repository;

import com.shorten_url.entity.Role;
import com.shorten_url.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(Role.USER);

        entityManager.persistAndFlush(user);
        entityManager.clear();
    }

    @Test
    void findByEmail_ShouldReturnUser() {
        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void existsByEmail_ShouldReturnTrue() {
        assertTrue(userRepository.existsByEmail("test@example.com"));
    }
}
