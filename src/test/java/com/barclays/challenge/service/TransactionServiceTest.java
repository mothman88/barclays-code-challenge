package com.barclays.challenge.service;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.barclays.challenge.exception.UnprocessableEntityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.barclays.challenge.exception.ForbiddenException;
import com.barclays.challenge.exception.ResourceNotFoundException;
import com.barclays.challenge.model.Account;
import com.barclays.challenge.model.Transaction;
import com.barclays.challenge.repository.AccountRepository;
import com.barclays.challenge.repository.TransactionRepository;

public class TransactionServiceTest {

    private TransactionRepository transactionRepo;
    private AccountRepository accountRepo;
    private TransactionService transactionService;

    @BeforeEach
    public void setup() {
        transactionRepo = Mockito.mock(TransactionRepository.class);
        accountRepo = Mockito.mock(AccountRepository.class);
        transactionService = new TransactionService(transactionRepo, accountRepo);
    }

    @Test
    public void shouldCreateTransaction() {
        Account acc = new Account();
        acc.setAccountNumber("acc1");
        acc.setUserId("user1");
        acc.setBalance(50.0);
        when(accountRepo.findById("acc1")).thenReturn(Optional.of(acc));
        when(accountRepo.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction req = new Transaction();
        req.setType("deposit");
        req.setAmount(25.0);

        Transaction out = transactionService.createTransaction("acc1", req, "user1");
        assertEquals("user1", out.getUserId());
        assertEquals("acc1", out.getAccountNumber());
        assertEquals(75.0, acc.getBalance());
        verify(accountRepo).save(acc);
        verify(transactionRepo).save(req);
        assertNotNull(out.getId());
    }

    @Test
    public void shouldWithdrawButInsufficentFunds() {
        Account acc = new Account();
        acc.setAccountNumber("acc2");
        acc.setUserId("u");
        acc.setBalance(10.0);
        when(accountRepo.findById("acc2")).thenReturn(Optional.of(acc));

        Transaction req = new Transaction();
        req.setType("withdrawal");
        req.setAmount(20.0);

        assertThrows(UnprocessableEntityException.class, () -> transactionService.createTransaction("acc2", req, "u"));
    }

    @Test
    public void shouldNotCreateTransactionForDifferentUser() {
        Account acc = new Account();
        acc.setAccountNumber("acc3");
        acc.setUserId("owner");
        when(accountRepo.findById("acc3")).thenReturn(Optional.of(acc));

        Transaction req = new Transaction();
        req.setType("deposit");
        req.setAmount(5.0);

        assertThrows(ForbiddenException.class, () -> transactionService.createTransaction("acc3", req, "attacker"));
    }

    @Test
    public void shouldListTransactions() {
        Account acc = new Account();
        acc.setAccountNumber("acc4");
        acc.setUserId("u");
        when(accountRepo.findById("acc4")).thenReturn(Optional.of(acc));
        when(transactionRepo.findByAccountNumber("acc4")).thenReturn(Collections.emptyList());

        assertEquals(0, transactionService.listTransactions("acc4", "u").size());
        verify(transactionRepo).findByAccountNumber("acc4");
    }

    @Test
    public void shouldNotListTransactionsForUnknownAccount() {
        when(accountRepo.findById("missing")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> transactionService.listTransactions("missing", "u"));
    }

    @Test
    public void shouldGetTransactions() {
        Account acc = new Account();
        acc.setAccountNumber("acc5");
        acc.setUserId("owner");
        when(accountRepo.findById("acc5")).thenReturn(Optional.of(acc));

        Transaction tx = new Transaction();
        tx.setId("t1");
        tx.setAccountNumber("acc5");
        when(transactionRepo.findById("t1")).thenReturn(Optional.of(tx));

        Transaction out = transactionService.getTransaction("acc5", "t1", "owner");
        assertEquals("t1", out.getId());

        // tx not found
        when(transactionRepo.findById("missing")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> transactionService.getTransaction("acc5", "missing", "owner"));

        // account not found
        when(accountRepo.findById("noacct")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> transactionService.getTransaction("noacct", "t1", "owner"));

        // transaction-account mismatch
        Transaction other = new Transaction();
        other.setId("t2");
        other.setAccountNumber("diff");
        when(accountRepo.findById("acc6")).thenReturn(Optional.of(acc));
        when(transactionRepo.findById("t2")).thenReturn(Optional.of(other));
        assertThrows(ForbiddenException.class, () -> transactionService.getTransaction("acc6", "t2", "owner"));

        // forbidden due to wrong user
        Account accWrongUser = new Account();
        accWrongUser.setAccountNumber("acc7");
        accWrongUser.setUserId("someone");
        when(accountRepo.findById("acc7")).thenReturn(Optional.of(accWrongUser));
        when(transactionRepo.findById("t1")).thenReturn(Optional.of(tx));
        assertThrows(ForbiddenException.class, () -> transactionService.getTransaction("acc7", "t1", "owner"));
    }
}
