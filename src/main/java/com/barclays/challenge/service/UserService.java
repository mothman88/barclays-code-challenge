package com.barclays.challenge.service;

import com.barclays.challenge.dto.UserRequest;
import com.barclays.challenge.dto.UserResponse;
import com.barclays.challenge.exception.ResourceNotFoundException;
import com.barclays.challenge.model.User;
import com.barclays.challenge.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse createUser(UserRequest request) {
        User user = new User();
        user.setId("usr-" + UUID.randomUUID());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(request.getPassword()); // hash in real app
        user.setAddress(request.getAddress());
        userRepository.save(user);
        return mapToResponse(user);
    }

    public Optional<UserResponse> getUser(String userId) {
        return userRepository.findById(userId)
                .map(this::mapToResponse);
    }

    public UserResponse updateUser(String userId, UserRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found " + userId));
        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        userRepository.save(user);
        return mapToResponse(user);
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setAddress(user.getAddress());
        return response;
    }
}