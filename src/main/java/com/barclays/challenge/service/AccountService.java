package com.barclays.challenge.service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.barclays.challenge.exception.ForbiddenException;
import com.barclays.challenge.exception.ResourceNotFoundException;
import com.barclays.challenge.model.Account;
import com.barclays.challenge.repository.AccountRepository;

@Service
public class AccountService {
    private final AccountRepository accountRepo;

    public AccountService(AccountRepository accountRepo) {
        this.accountRepo = accountRepo;
    }

    public Account createAccount(String userId, Account request) {
        request.setAccountNumber("01" + new Random().nextInt(999999));
        request.setUserId(userId);
        request.setBalance(0.0);
        accountRepo.save(request);
        return request;
    }

    public List<Account> listAccounts(String userId) {
        return accountRepo.findByUserId(userId);
    }

    public Account getAccount(String accountNumber, String userId) {
        Account account = accountRepo.findById(accountNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found " + accountNumber));
        if (!account.getUserId().equals(userId)) throw new ForbiddenException("Forbidden");
        return account;
    }

    public Optional<Account> getAccount(String accountNumber) {
        return accountRepo.findById(accountNumber);
    }

    public Account updateAccount(String accountNumber, Account request, String userId) {
        Account account = getAccount(accountNumber, userId);
        account.setName(request.getName());
        account.setAccountType(request.getAccountType());
        accountRepo.save(account);
        return account;
    }

    public void deleteAccount(String accountNumber, String userId) {
        Account account = getAccount(accountNumber, userId);
        accountRepo.delete(account);
    }
}