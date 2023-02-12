package com.lazarev.paymentservice.service.impl;

import com.lazarev.model.event.OrderEvent;
import com.lazarev.model.event.OrderRejectEvent;
import com.lazarev.model.event.OrderStatusUpdateEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaOrderService {
    private final KafkaTemplate<Integer, OrderEvent> kafkaTemplate;

    @Value("${application.rejected-orders-topic}")
    private String rejectedOrderTopic;

    @Value("${application.updated-orders-topic}")
    private String updatedOrderTopic;

    public void sendMessageToRejectedOrderTopic(OrderRejectEvent event){
        ProducerRecord<Integer, OrderEvent> record = formRecord(event, rejectedOrderTopic);
        kafkaTemplate.send(record);
    }

    public void sendMessageToUpdatedOrderTopic(OrderStatusUpdateEvent event){
        ProducerRecord<Integer, OrderEvent> record = formRecord(event, updatedOrderTopic);
        kafkaTemplate.send(record);
    }

    private ProducerRecord<Integer, OrderEvent> formRecord(OrderEvent event, String topic){
        return new ProducerRecord<>(topic, event.getOrderId(), event);
    }
}
