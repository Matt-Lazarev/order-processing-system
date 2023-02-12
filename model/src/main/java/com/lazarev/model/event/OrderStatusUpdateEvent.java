package com.lazarev.model.event;

import com.lazarev.model.enums.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class OrderStatusUpdateEvent extends OrderEvent {
    private String message;
    public OrderStatusUpdateEvent(Integer orderId, Integer clientId,
                                  OrderStatus orderStatus,  String message) {
        super(orderId, clientId, orderStatus);
        this.message = message;
    }

    @Override
    public String toString() {
        return "OrderStatusUpdateEvent{" +
                "orderId=" + super.getOrderId() +
                "clientId=" + super.getClientId() +
                "newStatus='" + super.getOrderStatus() + "'" +
                "message='" + message  + "'" +
                '}';
    }
}
