package com.barclays.challenge.dto;

import com.barclays.challenge.model.Address;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    @NotNull
    private String name;
    @NotNull
    private String email;
    @NotNull
    private String phoneNumber;
    @NotNull
    private Address address;
    @NotNull
    private String password;
}