package com.barclays.challenge.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "bank_account")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    @Id
    private String accountNumber;

    @NotNull
    private String name;
    @NotNull
    private String accountType;
    private String sortCode;
    private double balance;
    private String currency;

    private String userId;
}