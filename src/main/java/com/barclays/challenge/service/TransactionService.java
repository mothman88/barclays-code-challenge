package com.barclays.challenge.service;

import com.barclays.challenge.exception.ForbiddenException;
import com.barclays.challenge.exception.ResourceNotFoundException;
import com.barclays.challenge.exception.UnprocessableEntityException;
import com.barclays.challenge.model.Account;
import com.barclays.challenge.model.Transaction;
import com.barclays.challenge.repository.AccountRepository;
import com.barclays.challenge.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepo;
    private final AccountRepository accountRepo;

    public TransactionService(TransactionRepository transactionRepo, AccountRepository accountRepo) {
        this.transactionRepo = transactionRepo;
        this.accountRepo = accountRepo;
    }

    public Transaction createTransaction(String accountNumber, Transaction request, String userId) {
        Account account = accountRepo.findById(accountNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found " + accountNumber));
        if (!account.getUserId().equals(userId)) throw new ForbiddenException("Forbidden");

        if (request.getType().equals("withdrawal") && account.getBalance() < request.getAmount()) {
            throw new UnprocessableEntityException("Insufficient funds");
        }

        if (request.getType().equals("deposit")) {
            account.setBalance(account.getBalance() + request.getAmount());
        } else {
            account.setBalance(account.getBalance() - request.getAmount());
        }

        accountRepo.save(account);
        request.setId(UUID.randomUUID().toString());
        request.setUserId(userId);
        request.setAccountNumber(accountNumber);
        transactionRepo.save(request);
        return request;
    }

    public List<Transaction> listTransactions(String accountNumber, String userId) {
        Account account = accountRepo.findById(accountNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (!account.getUserId().equals(userId)) throw new ForbiddenException("Forbidden");

        return transactionRepo.findByAccountNumber(accountNumber);
    }

    public Transaction getTransaction(String accountNumber, String transactionId, String userId) {
        Account account = accountRepo.findById(accountNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (!account.getUserId().equals(userId)) throw new ForbiddenException("Forbidden");

        Transaction tx = transactionRepo.findById(transactionId)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        if (!tx.getAccountNumber().equals(accountNumber)) throw new ForbiddenException("Transaction mismatch");
        return tx;
    }
}