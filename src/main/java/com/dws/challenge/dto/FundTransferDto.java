package com.dws.challenge.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class FundTransferDto {

    @NotNull
    private String accountFromId;

    @NotNull
    private String accountToId;

    @NotNull
    private BigDecimal amount;

    @Override
    public String toString() {
        return "FundTransferDto{" +
                "accountFromId='" + accountFromId + '\'' +
                ", accountToId='" + accountToId + '\'' +
                ", amount=" + amount +
                '}';
    }
}
