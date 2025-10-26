package com.barclays.challenge.controller;

import com.barclays.challenge.model.Transaction;
import com.barclays.challenge.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/v1/accounts/{accountNumber}/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@PathVariable String accountNumber, @RequestBody Transaction request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.createTransaction(accountNumber, request, principal.getName()));
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> listTransactions(@PathVariable String accountNumber, Principal principal) {
        return ResponseEntity.ok(transactionService.listTransactions(accountNumber, principal.getName()));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable String accountNumber, @PathVariable String transactionId, Principal principal) {
        return ResponseEntity.ok(transactionService.getTransaction(accountNumber, transactionId, principal.getName()));
    }
}