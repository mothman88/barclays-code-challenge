package com.barclays.challenge.service;

import com.barclays.challenge.dto.AuthRequest;
import com.barclays.challenge.dto.AuthResponse;
import com.barclays.challenge.model.User;
import com.barclays.challenge.repository.UserRepository;
import com.barclays.challenge.security.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse authenticate(AuthRequest request) {
        User user = userRepository.findByEmailAndPassword(request.getEmail(), request.getPassword())
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        String token = jwtUtil.generateToken(user.getId());
        return new AuthResponse(token);
    }
}