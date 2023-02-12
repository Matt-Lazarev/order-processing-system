package com.lazarev.paymentservice.service;

import com.lazarev.model.dto.payment.PaymentReserveResult;
import com.lazarev.model.enums.OrderStatus;
import com.lazarev.model.event.OrderPaymentAwaitEvent;
import com.lazarev.paymentservice.entity.Client;
import com.lazarev.paymentservice.repository.ClientRepository;
import com.lazarev.paymentservice.service.abst.ClientService;
import com.lazarev.paymentservice.service.impl.KafkaOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class ClientServiceTest {
    @Container
    @SuppressWarnings({"resource"})
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres")
            .withDatabaseName("payment_test_db")
            .withUsername("root")
            .withPassword("root");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dymDynamicPropertyRegistry) {
        dymDynamicPropertyRegistry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        dymDynamicPropertyRegistry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        dymDynamicPropertyRegistry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    ClientService clientService;

    @Autowired
    ClientRepository clientRepository;

    @MockBean
    KafkaOrderService kafkaOrderService;

    @Test
    void reservePayment_shouldReturnTrue_ifPaymentPossible(){
        OrderPaymentAwaitEvent event = createPositiveOrderPaymentAwaitEvent();

        PaymentReserveResult reservePaymentResult = clientService.reservePayment(event);
        Client client = clientRepository.findClientById(event.getClientId()).get();
        BigDecimal balanceAfterPayment = client.getBankAccount().getBalance();

        assertThat(reservePaymentResult.success()).isTrue();
        assertThat(reservePaymentResult.message()).isEqualTo("Order has been accepted");
        assertThat(balanceAfterPayment).isEqualTo(BigDecimal.valueOf(700).setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void reservePayment_shouldReturnTrue_ifNotEnoughMoneyOnBankAccount(){
        OrderPaymentAwaitEvent event = createNotEnoughMoneyOrderPaymentAwaitEvent();
        PaymentReserveResult reservePaymentResult = clientService.reservePayment(event);

        assertThat(reservePaymentResult.success()).isFalse();
        assertThat(reservePaymentResult.message()).isEqualTo("Not enough money to pay for the order");
    }

    @Test
    void reservePayment_shouldReturnTrue_ifClientNotFoundOnBankAccount(){
        OrderPaymentAwaitEvent event = createClientNotFoundOrderPaymentAwaitEvent();
        PaymentReserveResult reservePaymentResult = clientService.reservePayment(event);

        assertThat(reservePaymentResult.success()).isFalse();
        assertThat(reservePaymentResult.message()).isEqualTo("Client with id='10' not found");
    }

    private OrderPaymentAwaitEvent createPositiveOrderPaymentAwaitEvent(){
        OrderPaymentAwaitEvent event = new OrderPaymentAwaitEvent();
        event.setClientId(1);
        event.setOrderId(1);
        event.setOrderStatus(OrderStatus.PAYMENT_AWAIT);
        event.setTotalPrice(BigDecimal.valueOf(300));
        return event;
    }

    private OrderPaymentAwaitEvent createNotEnoughMoneyOrderPaymentAwaitEvent(){
        OrderPaymentAwaitEvent event = new OrderPaymentAwaitEvent();
        event.setClientId(1);
        event.setOrderId(1);
        event.setOrderStatus(OrderStatus.PAYMENT_AWAIT);
        event.setTotalPrice(BigDecimal.valueOf(2000));
        return event;
    }

    private OrderPaymentAwaitEvent createClientNotFoundOrderPaymentAwaitEvent(){
        OrderPaymentAwaitEvent event = new OrderPaymentAwaitEvent();
        event.setClientId(10);
        event.setOrderId(10);
        event.setOrderStatus(OrderStatus.PAYMENT_AWAIT);
        event.setTotalPrice(BigDecimal.valueOf(2000));
        return event;
    }
}