package com.lazarev.inventoryservice.listener;

import com.lazarev.inventoryservice.service.impl.InventoryServiceImpl;
import com.lazarev.model.enums.OrderStatus;
import com.lazarev.model.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryCheckTopicListener {
    private final InventoryServiceImpl inventoryService;

    @KafkaListener(topics = "${application.inventory-check-orders-topic}",
                   containerFactory = "containerFactory")
    public void listenInventoryCheckOrders(@Payload OrderInventoryCheckEvent event){
        log.info("new order_inventory_check event: {}", event);

        Integer orderId = event.getOrderId();
        Integer clientId = event.getClientId();
        inventoryService.sendUpdatedOrderEvent(orderId, clientId, "Checking the availability of order products", OrderStatus.INVENTORY_CHECK);

        boolean reserveResult = inventoryService.reserveOrder(event);
        if(!reserveResult){
            inventoryService.sendRejectedOrderEvent(orderId, clientId, "Not enough products in stock");
        }
    }
}
