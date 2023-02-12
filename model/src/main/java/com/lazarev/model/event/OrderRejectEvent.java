package com.lazarev.model.event;

import com.lazarev.model.enums.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class OrderRejectEvent extends OrderEvent{
    private String reason;

    public OrderRejectEvent(Integer orderId, Integer clientId,
                            OrderStatus orderStatus, String reason) {
        super(orderId, clientId, orderStatus);
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "OrderRejectedEvent{" +
                "orderId=" + super.getOrderId() +
                "clientId=" + super.getClientId() +
                "orderStatus='" + super.getOrderStatus() + "'" +
                "reason='" + reason  + "'" +
                '}';
    }
}
