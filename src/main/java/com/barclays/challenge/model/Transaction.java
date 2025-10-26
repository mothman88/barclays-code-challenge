package com.barclays.challenge.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transaction")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    private String id;
    @NotNull
    private String accountNumber;
    @NotNull
    private String userId;
    private double amount;
    private String currency;
    private String type;
    private String reference;
}