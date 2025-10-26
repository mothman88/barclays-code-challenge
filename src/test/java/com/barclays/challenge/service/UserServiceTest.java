package com.barclays.challenge.service;

import com.barclays.challenge.dto.UserRequest;
import com.barclays.challenge.dto.UserResponse;
import com.barclays.challenge.exception.ResourceNotFoundException;
import com.barclays.challenge.model.Address;
import com.barclays.challenge.model.User;
import com.barclays.challenge.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    public void setup() {
        userRepository = Mockito.mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    public void shouldCreateUser() {
        UserRequest req = new UserRequest();
        req.setName("Alice");
        req.setEmail("a@b.com");
        req.setPhoneNumber("123");
        req.setPassword("pw");
        req.setAddress(Address.builder()
                        .line1("line1")
                        .line2("line2")
                        .postcode("postcode")
                        .town("town")
                        .county("county")
                .build());

        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse resp = userService.createUser(req);

        assertNotNull(resp.getId());
        assertEquals("Alice", resp.getName());
        assertEquals("a@b.com", resp.getEmail());
        assertEquals("123", resp.getPhoneNumber());
        assertEquals("line1", resp.getAddress().getLine1());
        assertEquals("line2", resp.getAddress().getLine2());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void shouldGetUser() {
        User user = new User();
        user.setId("u1");
        user.setName("Bob");
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));

        Optional<UserResponse> res = userService.getUser("u1");
        assertTrue(res.isPresent());
        assertEquals("Bob", res.get().getName());
    }

    @Test
    public void shouldNotGetUser() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());
        Optional<UserResponse> res = userService.getUser("missing");
        assertFalse(res.isPresent());
    }

    @Test
    public void updateUser_updatesWhenExists() {
        User existing = new User();
        existing.setId("u2");
        existing.setName("Old");
        existing.setPhoneNumber("000");
        when(userRepository.findById("u2")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserRequest req = new UserRequest();
        req.setName("New");
        req.setPhoneNumber("111");

        UserResponse updated = userService.updateUser("u2", req);

        assertEquals("u2", updated.getId());
        assertEquals("New", updated.getName());
        assertEquals("111", updated.getPhoneNumber());
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void shouldNotUpdateNotExistingUser() {
        when(userRepository.findById("no")).thenReturn(Optional.empty());
        UserRequest req = new UserRequest();
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser("no", req));
    }

    @Test
    public void shouldDeleteUser() {
        doNothing().when(userRepository).deleteById("u3");
        userService.deleteUser("u3");
        verify(userRepository).deleteById("u3");
    }
}