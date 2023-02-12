package com.lazarev.model.event;

import com.lazarev.model.enums.OrderStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
public class OrderPaymentAwaitEvent extends OrderEvent {
    private BigDecimal totalPrice;

    public OrderPaymentAwaitEvent(Integer orderId, Integer clientId,
                                  OrderStatus orderStatus, BigDecimal totalPrice) {
        super(orderId, clientId, orderStatus);
        this.totalPrice = totalPrice;
    }

    @Override
    public String toString() {
        return "OrderPaymentAwaitEvent{" +
                "orderId=" + super.getOrderId() +
                "clientId=" + super.getClientId() +
                "orderStatus='" + super.getOrderStatus() + "'" +
                "totalPrice=" + totalPrice +
                '}';
    }
}
