package com.barclays.challenge.repository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.barclays.challenge.model.Transaction;

@DataJpaTest
public class TransactionRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    public void findByAccountNumber_returnsTransactions_forGivenAccount() {
        Transaction t1 = new Transaction();
        t1.setAccountNumber("ACC-1");
        t1.setType("deposit");
        t1.setAmount(10.0);
        t1.setUserId("u1");

        Transaction t2 = new Transaction();
        t2.setAccountNumber("ACC-1");
        t2.setType("withdrawal");
        t2.setAmount(5.0);
        t2.setUserId("u1");

        Transaction tOther = new Transaction();
        tOther.setAccountNumber("ACC-2");
        tOther.setType("deposit");
        tOther.setAmount(7.0);
        tOther.setUserId("u2");

        entityManager.persist(t1);
        entityManager.persist(t2);
        entityManager.persist(tOther);
        entityManager.flush();

        List<Transaction> results = transactionRepository.findByAccountNumber("ACC-1");
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(tx -> "ACC-1".equals(tx.getAccountNumber())));
    }

    @Test
    public void findByAccountNumber_returnsEmpty_forUnknownAccount() {
        List<Transaction> results = transactionRepository.findByAccountNumber("NONEXISTENT");
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
}
