package com.barclays.challenge.service;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.barclays.challenge.exception.ForbiddenException;
import com.barclays.challenge.exception.ResourceNotFoundException;
import com.barclays.challenge.model.Account;
import com.barclays.challenge.repository.AccountRepository;

public class AccountServiceTest {

    private AccountRepository accountRepo;
    private AccountService accountService;

    @BeforeEach
    public void setup() {
        accountRepo = Mockito.mock(AccountRepository.class);
        accountService = new AccountService(accountRepo);
    }

    @Test
    public void shouldCreateAccount() {
        Account req = new Account();
        req.setName("acct");
        when(accountRepo.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account created = accountService.createAccount("user42", req);

        assertNotNull(created.getAccountNumber());
        assertTrue(created.getAccountNumber().startsWith("01"));
        assertEquals("user42", created.getUserId());
        assertEquals(0.0, created.getBalance());
        verify(accountRepo).save(created);
    }

    @Test
    public void shouldListAccounts() {
        when(accountRepo.findByUserId("u")).thenReturn(Collections.emptyList());
        assertEquals(0, accountService.listAccounts("u").size());
        verify(accountRepo).findByUserId("u");
    }

    @Test
    public void shouldGetAccount() {
        Account a = new Account();
        a.setAccountNumber("acc1");
        a.setUserId("owner");
        when(accountRepo.findById("acc1")).thenReturn(Optional.of(a));

        Account res = accountService.getAccount("acc1", "owner");
        assertEquals("acc1", res.getAccountNumber());
    }

    @Test
    public void shouldNotRetrieveNotExistingAccount() {
        when(accountRepo.findById("x")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> accountService.getAccount("x", "any"));
    }

    @Test
    public void shouldNotBeAllowToRetrieveAccount() {
        Account a = new Account();
        a.setAccountNumber("acc2");
        a.setUserId("owner");
        when(accountRepo.findById("acc2")).thenReturn(Optional.of(a));
        assertThrows(ForbiddenException.class, () -> accountService.getAccount("acc2", "other"));
    }

    @Test
    public void shouldUpdateAccount() {
        Account stored = new Account();
        stored.setAccountNumber("acc3");
        stored.setUserId("me");
        stored.setName("old");
        stored.setAccountType("A");
        when(accountRepo.findById("acc3")).thenReturn(Optional.of(stored));
        when(accountRepo.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account request = new Account();
        request.setName("new");
        request.setAccountType("B");

        Account updated = accountService.updateAccount("acc3", request, "me");
        assertEquals("new", updated.getName());
        assertEquals("B", updated.getAccountType());
        verify(accountRepo).save(stored);
    }

    @Test
    public void shouldDeleteAccount() {
        Account stored = new Account();
        stored.setAccountNumber("acc4");
        stored.setUserId("me");
        when(accountRepo.findById("acc4")).thenReturn(Optional.of(stored));
        doNothing().when(accountRepo).delete(stored);

        accountService.deleteAccount("acc4", "me");
        verify(accountRepo).delete(stored);
    }
}