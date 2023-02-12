package com.lazarev.model.dto.order;

import com.lazarev.model.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(Integer id,
                            Integer clientId,
                            LocalDateTime createdAt,
                            String message,
                            OrderStatus status,
                            List<OrderItemDto> orderItems) { }
