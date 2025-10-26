package com.barclays.challenge.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.barclays.challenge.dto.AuthRequest;
import com.barclays.challenge.dto.AuthResponse;
import com.barclays.challenge.model.User;
import com.barclays.challenge.repository.UserRepository;
import com.barclays.challenge.security.JwtUtil;

public class AuthServiceTest {

    private UserRepository userRepository;
    private JwtUtil jwtUtil;
    private AuthService authService;

    @BeforeEach
    public void setup() {
        userRepository = Mockito.mock(UserRepository.class);
        jwtUtil = Mockito.mock(JwtUtil.class);
        authService = new AuthService(userRepository, jwtUtil);
    }

    @Test
    public void shouldAuthenticate() {
        User user = new User();
        user.setId("uid");
        user.setPassword("secret");
        user.setEmail("e@e.com");
        when(userRepository.findByEmailAndPassword("e@e.com", "secret")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("uid")).thenReturn("tok");

        AuthRequest req = new AuthRequest();
        req.setEmail("e@e.com");
        req.setPassword("secret");

        AuthResponse resp = authService.authenticate(req);
        assertNotNull(resp);
        assertEquals("tok", resp.getToken());
        verify(jwtUtil).generateToken("uid");
    }

    @Test
    public void shouldNotAuthenticateWhenUserNotExists() {
        when(userRepository.findByEmailAndPassword("no", "nopwd")).thenReturn(Optional.empty());
        AuthRequest req = new AuthRequest();
        req.setEmail("no");
        req.setPassword("x");
        assertThrows(RuntimeException.class, () -> authService.authenticate(req));
    }

}