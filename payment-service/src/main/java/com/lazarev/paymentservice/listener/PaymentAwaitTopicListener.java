package com.lazarev.paymentservice.listener;

import com.lazarev.model.dto.payment.PaymentReserveResult;
import com.lazarev.model.enums.OrderStatus;
import com.lazarev.model.event.OrderPaymentAwaitEvent;
import com.lazarev.paymentservice.service.abst.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentAwaitTopicListener {
    private final ClientService clientService;

    @KafkaListener(topics = "${application.payment-await-orders-topic}",
                   containerFactory = "containerFactory")
    public void listenInventoryCheckOrders(@Payload OrderPaymentAwaitEvent event){
        log.info("new payment_await event to check: {}", event);

        Integer orderId = event.getOrderId();
        Integer clientId = event.getClientId();;
        clientService.sendUpdatedOrderEvent(orderId, clientId, "Checking the availability of order products", OrderStatus.PAYMENT_AWAIT);

        PaymentReserveResult checkResult = clientService.reservePayment(event);
        if(!checkResult.success()){
            clientService.sendRejectedOrderEvent(orderId, clientId, checkResult.message());
        }
        else {
            clientService.sendUpdatedOrderEvent(orderId, clientId, "Order has been accepted", OrderStatus.ACCEPTED);
        }
    }
}
