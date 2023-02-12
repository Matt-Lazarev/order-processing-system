package com.lazarev.orderservice.service.abst;

import com.lazarev.model.dto.order.OrderPlaceRequest;
import com.lazarev.model.dto.order.OrderResponse;
import com.lazarev.model.event.OrderRejectEvent;
import com.lazarev.model.event.OrderStatusUpdateEvent;

import java.util.List;

public interface OrderService {
    List<OrderResponse> getAllOrdersByClientId(Integer clientId);

    OrderResponse getOrderById(Integer orderId);

    void placeNewOrder(OrderPlaceRequest orderPlaceRequest);

    void updateOrderStatus(OrderStatusUpdateEvent event);

    void updateOrderToRejected(OrderRejectEvent event);
}
