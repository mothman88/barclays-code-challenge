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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.barclays.challenge.model.Transaction;
import com.barclays.challenge.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper mapper;

    @Test
    @WithMockUser(username = "test-user")
    public void createTransaction_withAuthenticatedUser_returnsCreated() throws Exception {
        Transaction req = new Transaction();
        Transaction resp = new Transaction();
        when(transactionService.createTransaction(eq("acc1"), any(Transaction.class), eq("test-user"))).thenReturn(resp);

        mockMvc.perform(post("/v1/accounts/acc1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "test-user")
    public void listTransactions_withAuthenticatedUser_returnsOk() throws Exception {
        when(transactionService.listTransactions("acc1", "test-user")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/v1/accounts/acc1/transactions"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test-user")
    public void getTransaction_withAuthenticatedUser_returnsOk() throws Exception {
        Transaction resp = new Transaction();
        when(transactionService.getTransaction("acc1", "tx1", "test-user")).thenReturn(resp);

        mockMvc.perform(get("/v1/accounts/acc1/transactions/tx1"))
                .andExpect(status().isOk());
    }
}