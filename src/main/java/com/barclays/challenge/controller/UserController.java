package com.barclays.challenge.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barclays.challenge.dto.UserRequest;
import com.barclays.challenge.dto.UserResponse;
import com.barclays.challenge.service.UserService;

@RestController
@RequestMapping("/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String userId) {
        return userService
                .getUser(userId)
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable String userId, @RequestBody UserRequest request) {
        return userService
                .getUser(userId)
                .map(user -> userService.updateUser(userId, request))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}