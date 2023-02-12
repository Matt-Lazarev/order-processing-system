package com.lazarev.orderservice.listener;

import com.lazarev.model.event.OrderRejectEvent;
import com.lazarev.orderservice.service.impl.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RejectedOrderTopicListener {
    private final OrderServiceImpl orderService;

    @KafkaListener(topics = "${application.rejected-orders-topic}", containerFactory = "containerFactory")
    public void listenRejectedOrdersTopic(@Payload OrderRejectEvent event){
        log.info("new order_rejected event to check: {}", event);
        orderService.updateOrderToRejected(event);
    }
}
