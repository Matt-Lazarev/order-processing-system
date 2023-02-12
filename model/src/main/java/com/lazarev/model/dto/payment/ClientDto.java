package com.lazarev.model.dto.payment;

public record ClientDto(String firstname,
                        String lastname,
                        String email,
                        BankAccountDto bankAccount) { }
