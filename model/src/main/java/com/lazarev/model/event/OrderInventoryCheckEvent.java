package com.lazarev.model.event;

import com.lazarev.model.dto.order.OrderItemDto;
import com.lazarev.model.enums.OrderStatus;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class OrderInventoryCheckEvent extends OrderEvent {
    private List<OrderItemDto> orderItems;

    public OrderInventoryCheckEvent(Integer orderId, Integer clientId,
                                    OrderStatus orderStatus, List<OrderItemDto> orderItems) {
        super(orderId, clientId, orderStatus);
        this.orderItems = orderItems;
    }

    @Override
    public String toString() {
        return "OrderInventoryCheckEvent{" +
                "orderId=" + super.getOrderId() +
                "clientId=" + super.getClientId() +
                "orderStatus='" + super.getOrderStatus() + "'" +
                "orderItems=" + orderItems +
                '}';
    }
}
