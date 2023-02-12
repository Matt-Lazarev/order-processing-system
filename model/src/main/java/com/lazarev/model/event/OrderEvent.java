package com.lazarev.model.event;

import com.lazarev.model.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class OrderEvent {
    private Integer orderId;
    private Integer clientId;
    private OrderStatus orderStatus;
}
