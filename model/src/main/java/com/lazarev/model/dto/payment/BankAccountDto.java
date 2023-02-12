package com.lazarev.model.dto.payment;

import java.math.BigDecimal;

public record BankAccountDto(String number,
                             BigDecimal balance) {
}
