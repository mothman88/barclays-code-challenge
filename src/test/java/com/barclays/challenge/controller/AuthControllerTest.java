package com.barclays.challenge.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.barclays.challenge.dto.AuthResponse;
import com.barclays.challenge.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AuthControllerTest {

    private MockMvc mockMvc;
    private AuthService authService;
    private ObjectMapper mapper;

    @BeforeEach
    public void setup() {
        authService = Mockito.mock(AuthService.class);
        AuthController controller = new AuthController(authService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        mapper = new ObjectMapper();
    }

    @Test
    public void shouldLogin() throws Exception {
        AuthResponse resp = new AuthResponse();
        when(authService.authenticate(any())).thenReturn(resp);

        String json = "{\"email\":\"user\",\"password\":\"pass\"}";

        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        verify(authService).authenticate(any());
    }

}
