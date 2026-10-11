package com.lockedin.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreditBalanceResponse {
    private Long userId;
    private int balance;
    private boolean lowBalance;
}
