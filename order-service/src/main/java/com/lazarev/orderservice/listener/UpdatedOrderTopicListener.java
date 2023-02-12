package com.lazarev.orderservice.listener;

import com.lazarev.model.event.OrderStatusUpdateEvent;
import com.lazarev.orderservice.service.impl.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdatedOrderTopicListener {
    private final OrderServiceImpl orderService;

    @KafkaListener(topics = "${application.updated-orders-topic}",
                   containerFactory = "containerFactory")
    public void listenUpdatedOrderTopic(@Payload OrderStatusUpdateEvent event){
        log.info("new order_status_updated event to check: {}", event);
        orderService.updateOrderStatus(event);
    }
}
