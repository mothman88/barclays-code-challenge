package com.barclays.challenge.repository;

import com.barclays.challenge.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, String> {
    List<Account> findByUserId(String userId);
}