package com.lazarev.model.dto.inventory;

import java.math.BigDecimal;

public record ProductDto (
        String code, String name,
        BigDecimal price, Integer amount) { }
