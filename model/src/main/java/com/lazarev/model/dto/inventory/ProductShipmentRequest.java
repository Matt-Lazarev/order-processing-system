package com.lazarev.model.dto.inventory;

public record ProductShipmentRequest (
        String productCode, Integer amount) { }
