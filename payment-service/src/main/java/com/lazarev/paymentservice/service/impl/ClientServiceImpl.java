package com.lazarev.paymentservice.service.impl;

import com.lazarev.model.dto.payment.BankAccountDto;
import com.lazarev.model.dto.payment.ClientDto;
import com.lazarev.model.dto.payment.PaymentReserveResult;
import com.lazarev.model.enums.OrderStatus;
import com.lazarev.model.event.OrderPaymentAwaitEvent;
import com.lazarev.model.event.OrderRejectEvent;
import com.lazarev.model.event.OrderStatusUpdateEvent;
import com.lazarev.paymentservice.entity.BankAccount;
import com.lazarev.paymentservice.entity.Client;
import com.lazarev.paymentservice.exception.ClientNotFoundException;
import com.lazarev.paymentservice.repository.ClientRepository;
import com.lazarev.paymentservice.service.abst.ClientService;
import com.lazarev.paymentservice.service.mapper.ClientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final KafkaOrderService kafkaOrderService;
    private final ClientMapper clientMapper;

    @Override
    @Transactional(readOnly = true)
    public ClientDto getClientById(Integer id) {
        Client client = clientRepository.findClientById(id)
                .orElseThrow(()->new ClientNotFoundException("Client with id='%d' not found".formatted(id)));
        return clientMapper.toClientDto(client);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentReserveResult checkPaymentPossibility(Integer clientId, BigDecimal amount){
        Client client = clientRepository.findClientById(clientId)
                .orElseThrow(()->new ClientNotFoundException("Client with id='%d' not found".formatted(clientId)));

        BankAccount clientBankAccount = client.getBankAccount();
        if(clientBankAccount == null){
            return new PaymentReserveResult(false, "Bank Account is not attached");
        }

        boolean isPurchasePossible = clientBankAccount.getBalance().compareTo(amount) > 0;
        if(!isPurchasePossible){
            return new PaymentReserveResult(false, "Not enough money");
        }

        return new PaymentReserveResult(true, "Payment is possible");
    }

    @Override
    @Transactional
    public void saveClient(ClientDto clientDto) {
        Client client = clientMapper.toClient(clientDto);
        clientRepository.save(client);
    }

    @Override
    @Transactional
    public void attachBankAccount(Integer clientId, BankAccountDto bankAccountDto) {
        Client client = clientRepository.findClientById(clientId)
                .orElseThrow(()->new ClientNotFoundException("Client with id='%d' not found".formatted(clientId)));
        BankAccount bankAccount = clientMapper.toBankAccount(bankAccountDto);
        client.setBankAccount(bankAccount);
        clientRepository.save(client);
    }

    @Override
    @Transactional
    public void detachBankAccount(Integer clientId) {
        Client client = clientRepository.findClientById(clientId)
                .orElseThrow(()->new ClientNotFoundException("Client with id='%d' not found".formatted(clientId)));
        client.setBankAccount(null);
        clientRepository.save(client);
    }

    @Override
    @Transactional
    public PaymentReserveResult reservePayment(OrderPaymentAwaitEvent event){
        Optional<Client> clientOptional = clientRepository.findClientById(event.getClientId());

        if(clientOptional.isEmpty()){
            return new PaymentReserveResult(false, "Client with id='%d' not found".formatted(event.getClientId()));
        }

        Client client = clientOptional.get();
        BankAccount clientBankAccount = client.getBankAccount();

        if(clientBankAccount == null){
            return new PaymentReserveResult(false, "Bank Account is not attached");
        }

        boolean isPurchasePossible = clientBankAccount.getBalance().compareTo(event.getTotalPrice()) > 0;
        if(isPurchasePossible){
            clientBankAccount.setBalance(clientBankAccount.getBalance().subtract(event.getTotalPrice()));
            return new PaymentReserveResult(true, "Order has been accepted");
        }
        return new PaymentReserveResult(false, "Not enough money to pay for the order");
    }

    @Override
    @Transactional
    public void sendUpdatedOrderEvent(Integer orderId, Integer clientId, String message, OrderStatus newStatus) {
        OrderStatusUpdateEvent updatedEvent = new OrderStatusUpdateEvent(orderId, clientId, newStatus, message);
        kafkaOrderService.sendMessageToUpdatedOrderTopic(updatedEvent);
    }

    @Override
    @Transactional
    public void sendRejectedOrderEvent(Integer orderId, Integer clientId, String reason) {
        OrderRejectEvent rejectEvent = new OrderRejectEvent(orderId, clientId, OrderStatus.REJECTED, reason);
        kafkaOrderService.sendMessageToRejectedOrderTopic(rejectEvent);
    }
}
