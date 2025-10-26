package com.barclays.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {
    private String accountNumber;
    private String sortCode;
    private String name;
    private String accountType;
    private double balance;
    private String currency;
    private String userId;
}