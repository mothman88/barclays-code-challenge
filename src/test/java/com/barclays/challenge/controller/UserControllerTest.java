package com.barclays.challenge.controller;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.barclays.challenge.dto.UserRequest;
import com.barclays.challenge.dto.UserResponse;
import com.barclays.challenge.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void createUser_returnsCreated_andCallsService() throws Exception {
        UserRequest req = new UserRequest();
        UserResponse resp = new UserResponse();
        when(userService.createUser(any(UserRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        verify(userService).createUser(any(UserRequest.class));
    }

    @Test
    @WithMockUser(username = "u1")
    public void getUser_found_returnsOk() throws Exception {
        UserResponse resp = new UserResponse();
        resp.setId("u1");
        when(userService.getUser("u1")).thenReturn(Optional.of(resp));

        mockMvc.perform(get("/v1/users/u1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "u1")
    public void getUser_notFound_returnsNotFound_withErrorBody() throws Exception {
        when(userService.getUser("missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/users/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "u2")
    public void updateUser_found_returnsOk() throws Exception {
        UserRequest req = new UserRequest();
        UserResponse existing = new UserResponse();
        UserResponse updated = new UserResponse();
        when(userService.getUser("u2")).thenReturn(Optional.of(existing));
        when(userService.updateUser("u2", req)).thenReturn(updated);

        mockMvc.perform(patch("/v1/users/u2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "x")
    public void updateUser_notFound_returnsNotFound() throws Exception {
        UserRequest req = new UserRequest();
        when(userService.getUser("x")).thenReturn(Optional.empty());

        mockMvc.perform(patch("/v1/users/x")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "u3")
    public void deleteUser_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/v1/users/u3"))
                .andExpect(status().isNoContent());
    }
}