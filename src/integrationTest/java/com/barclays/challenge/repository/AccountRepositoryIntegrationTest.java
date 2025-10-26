package com.barclays.challenge.repository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.barclays.challenge.model.Account;

@DataJpaTest
public class AccountRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    public void findByUserId_returnsAccounts_forGivenUser() {
        Account a1 = new Account();
        a1.setAccountNumber("ACC-100");
        a1.setUserId("userA");
        a1.setName("Savings");
        a1.setAccountType("SAV");
        a1.setBalance(100.0);

        Account a2 = new Account();
        a2.setAccountNumber("ACC-101");
        a2.setUserId("userA");
        a2.setName("Checking");
        a2.setAccountType("CHK");
        a2.setBalance(200.0);

        Account other = new Account();
        other.setAccountNumber("ACC-200");
        other.setUserId("userB");
        other.setName("Other");
        other.setAccountType("SAV");
        other.setBalance(50.0);

        entityManager.persist(a1);
        entityManager.persist(a2);
        entityManager.persist(other);
        entityManager.flush();

        List<Account> results = accountRepository.findByUserId("userA");
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    public void findByUserId_returnsEmpty_forUnknownUser() {
        List<Account> results = accountRepository.findByUserId("no-such-user");
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void findById_returnsAccount_whenPresent() {
        Account a = new Account();
        a.setAccountNumber("ACC-300");
        a.setUserId("uX");
        a.setName("Single");
        a.setAccountType("SAV");
        a.setBalance(10.0);

        entityManager.persist(a);
        entityManager.flush();

        Account found = accountRepository.findById("ACC-300").orElse(null);
        assertNotNull(found);
        assertEquals("ACC-300", found.getAccountNumber());
        assertEquals("uX", found.getUserId());
    }
}