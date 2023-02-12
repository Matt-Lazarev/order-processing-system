package com.lazarev.orderservice.service.impl;

import com.lazarev.model.dto.order.OrderPlaceRequest;
import com.lazarev.model.dto.order.OrderResponse;
import com.lazarev.model.enums.OrderStatus;
import com.lazarev.model.event.OrderInventoryCheckEvent;
import com.lazarev.model.event.OrderRejectEvent;
import com.lazarev.model.event.OrderStatusUpdateEvent;
import com.lazarev.orderservice.entity.Order;
import com.lazarev.orderservice.exception.OrderNotFoundException;
import com.lazarev.orderservice.repository.OrderRepository;
import com.lazarev.orderservice.service.abst.OrderService;
import com.lazarev.orderservice.service.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service

@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final KafkaOrderService kafkaOrderService;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrdersByClientId(Integer clientId) {
        List<Order> orders = orderRepository.findAllOrdersByClientId(clientId);
        return orderMapper.toOrderResponseList(orders);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Integer orderId) {
        Order order = orderRepository.findOrderById(orderId)
                .orElseThrow(()->new OrderNotFoundException("Order with id='%d' not found".formatted(orderId)));
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public void placeNewOrder(OrderPlaceRequest orderPlaceRequest){
        Order order = orderMapper.toOrder(orderPlaceRequest);
        Order savedOrder = orderRepository.save(order);

        OrderInventoryCheckEvent event = orderMapper.toOrderInventoryCheckEvent(savedOrder.getId(), orderPlaceRequest);
        kafkaOrderService.sendMessageToInventoryCheckTopic(event);
    }

    @Override
    @Transactional
    public void updateOrderStatus(OrderStatusUpdateEvent event) {
        orderRepository.updateOrderStatusAndMessage(event.getOrderId(), event.getOrderStatus(), event.getMessage());
    }

    @Override
    @Transactional
    public void updateOrderToRejected(OrderRejectEvent event) {
        orderRepository.updateOrderStatusAndMessage(event.getOrderId(), OrderStatus.REJECTED, event.getReason());
    }
}
