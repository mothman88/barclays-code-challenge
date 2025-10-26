package com.barclays.challenge.repository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.barclays.challenge.model.Address;
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
        u.setId(UUID.randomUUID().toString());
        u.setEmail("test@example.com");
        u.setPassword("secret");
        u.setName("Tester");
        u.setPhoneNumber("+07777777");
        u.setAddress(Address.builder()
                        .line1("line1")
                        .line2("line2")
                        .postcode("postcode")
                        .town("town")
                        .county("county")
                .build());
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