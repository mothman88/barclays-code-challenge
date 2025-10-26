package com.barclays.challenge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountRequest {
    @NotNull
    private String name;
    @NotNull
    private String accountType; // e.g., "personal"
}