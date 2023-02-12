package com.lazarev.orderservice.service.mapper;

import com.lazarev.model.dto.order.OrderPlaceRequest;
import com.lazarev.model.dto.order.OrderItemDto;
import com.lazarev.model.dto.order.OrderResponse;
import com.lazarev.model.enums.OrderStatus;
import com.lazarev.model.event.OrderInventoryCheckEvent;
import com.lazarev.orderservice.entity.Order;
import com.lazarev.orderservice.entity.OrderItem;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    Order toOrder(OrderPlaceRequest orderDto);

    OrderItem toOrderItem(OrderItemDto orderItemDto);

    OrderResponse toOrderResponse(Order order);

    @AfterMapping
    default void setOrderStatus(@MappingTarget Order order){
        order.setStatus(OrderStatus.NEW);
    }

    @AfterMapping
    default void setCreatedAt(@MappingTarget Order order){
        order.setCreatedAt(LocalDateTime.now());
    }

    default OrderInventoryCheckEvent toOrderInventoryCheckEvent(Integer orderId, OrderPlaceRequest orderPlaceRequest){
        return new OrderInventoryCheckEvent(
                orderId, orderPlaceRequest.clientId(),
                OrderStatus.INVENTORY_CHECK, orderPlaceRequest.orderItems()
        );
    }

    default List<OrderResponse> toOrderResponseList(List<Order> orders){
        return orders.stream().map(this::toOrderResponse).toList();
    }
}
