package com.lazarev.orderservice.service.impl;

import com.lazarev.model.event.OrderInventoryCheckEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaOrderService {
    private final KafkaTemplate<Integer, OrderInventoryCheckEvent> kafkaTemplate;

    @Value("${application.inventory-check-orders-topic}")
    private String orderTopic;

    public void sendMessageToInventoryCheckTopic(OrderInventoryCheckEvent event){
        ProducerRecord<Integer, OrderInventoryCheckEvent> record = formRecord(event);
        kafkaTemplate.send(record);
        log.info("event published: {}", event);
    }

    private ProducerRecord<Integer, OrderInventoryCheckEvent> formRecord(OrderInventoryCheckEvent event){
        return new ProducerRecord<>(orderTopic, event.getOrderId(), event);
    }
}
