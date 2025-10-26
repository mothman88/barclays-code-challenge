package com.barclays.challenge.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @NotNull
    private String line1;
    private String line2;
    @NotNull
    private String town;
    private String county;
    @NotNull
    private String postcode;
}