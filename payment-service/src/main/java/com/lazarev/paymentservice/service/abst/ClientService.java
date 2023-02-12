package com.lazarev.paymentservice.service.abst;

import com.lazarev.model.dto.payment.BankAccountDto;
import com.lazarev.model.dto.payment.ClientDto;
import com.lazarev.model.dto.payment.PaymentReserveResult;
import com.lazarev.model.enums.OrderStatus;
import com.lazarev.model.event.OrderPaymentAwaitEvent;

import java.math.BigDecimal;

public interface ClientService {
    ClientDto getClientById(Integer id);

    PaymentReserveResult checkPaymentPossibility(Integer clientId, BigDecimal amount);

    void saveClient(ClientDto clientDto);

    void attachBankAccount(Integer clientId, BankAccountDto bankAccountDto);

    void detachBankAccount(Integer clientId);

    PaymentReserveResult reservePayment(OrderPaymentAwaitEvent event);

    void sendUpdatedOrderEvent(Integer orderId, Integer clientId, String message, OrderStatus newStatus);

    void sendRejectedOrderEvent(Integer orderId, Integer clientId, String reason);
}
