package com.barclays.challenge.controller;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.barclays.challenge.dto.AccountRequest;
import com.barclays.challenge.model.Account;
import com.barclays.challenge.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper mapper;

    @Test
    @WithMockUser(username = "test-user")
    public void shouldListAccounts() throws Exception {
        when(accountService.listAccounts("test-user")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/v1/accounts"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test-user")
    public void shouldCreateAccount() throws Exception {
        AccountRequest req = new AccountRequest();
        req.setAccountType("personal");
        req.setName("account name");
        Account resp = new Account();
        when(accountService.createAccount(any(String.class), any(Account.class))).thenReturn(resp);

        mockMvc.perform(post("/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "test-user")
    public void shouldGetAccount() throws Exception {
        Account resp = new Account();
        when(accountService.getAccount("acc1", "test-user")).thenReturn(resp);

        mockMvc.perform(get("/v1/accounts/acc1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test-user")
    public void shouldUpdateAccount() throws Exception {
        Account req = new Account();
        req.setAccountType("personal");
        req.setName("account name");
        Account updated = new Account();
        when(accountService.updateAccount(eq("acc2"), any(Account.class), eq("test-user"))).thenReturn(updated);

        mockMvc.perform(patch("/v1/accounts/acc2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test-user")
    public void shouldDeleteAccount() throws Exception {
        mockMvc.perform(delete("/v1/accounts/acc3"))
                .andExpect(status().isNoContent());
    }
}