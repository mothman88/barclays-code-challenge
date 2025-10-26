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
public class TransactionRequest {
    @NotNull
    private double amount;
    private String currency; // e.g., "GBP"
    private String type;     // "deposit" or "withdrawal"
    private String reference;
}