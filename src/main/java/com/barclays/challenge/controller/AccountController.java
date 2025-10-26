package com.barclays.challenge.controller;

import com.barclays.challenge.model.Account;
import com.barclays.challenge.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/v1/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@Valid @RequestBody Account request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(principal.getName(), request));
    }

    @GetMapping
    public ResponseEntity<List<Account>> listAccounts(Principal principal) {
        return ResponseEntity.ok(accountService.listAccounts(principal.getName()));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<Account> getAccount(@PathVariable String accountNumber, Principal principal) {
        return ResponseEntity.ok(accountService.getAccount(accountNumber, principal.getName()));
    }

    @PatchMapping("/{accountNumber}")
    public ResponseEntity<Account> updateAccount(@PathVariable String accountNumber, @Valid @RequestBody Account request, Principal principal) {
        return ResponseEntity.ok(accountService.updateAccount(accountNumber, request, principal.getName()));
    }

    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountNumber, Principal principal) {
        accountService.deleteAccount(accountNumber, principal.getName());
        return ResponseEntity.noContent().build();
    }
}