package com.lazarev.model.dto.payment;

public record PaymentReserveResult(
        Boolean success,
        String message) { }
