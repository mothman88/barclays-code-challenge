package com.barclays.challenge.repository;

import com.barclays.challenge.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmailAndPassword(String email, String password);
}