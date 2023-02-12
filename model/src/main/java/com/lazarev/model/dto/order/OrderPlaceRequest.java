package com.lazarev.model.dto.order;

import java.util.List;

public record OrderPlaceRequest(
    Integer clientId,
    List<OrderItemDto> orderItems) {}
