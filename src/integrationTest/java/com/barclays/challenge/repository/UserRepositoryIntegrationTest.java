package com.barclays.challenge.repository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.barclays.challenge.model.User;

@DataJpaTest
public class UserRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void findByEmailAndPassword_returnsUser_whenPresent() {
        User u = new User();
        u.setEmail("test@example.com");
        u.setPassword("secret");
        u.setName("Tester");
        // persist
        entityManager.persistAndFlush(u);

        Optional<User> found = userRepository.findByEmailAndPassword("test@example.com", "secret");
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
        assertEquals("Tester", found.get().getName());
    }

    @Test
    public void findByEmailAndPassword_returnsEmpty_whenMissing() {
        Optional<User> found = userRepository.findByEmailAndPassword("nope@example.com", "x");
        assertFalse(found.isPresent());
    }
}