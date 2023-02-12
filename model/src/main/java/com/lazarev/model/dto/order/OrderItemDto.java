package com.lazarev.model.dto.order;

public record OrderItemDto (
    String productCode,
    Integer amount) {}
