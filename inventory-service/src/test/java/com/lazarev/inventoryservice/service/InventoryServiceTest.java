package com.lazarev.inventoryservice.service;

import com.lazarev.inventoryservice.repository.InventoryRepository;
import com.lazarev.inventoryservice.service.impl.InventoryServiceImpl;
import com.lazarev.inventoryservice.service.impl.KafkaOrderService;
import com.lazarev.model.dto.order.OrderItemDto;
import com.lazarev.model.enums.OrderStatus;
import com.lazarev.model.event.OrderInventoryCheckEvent;
import com.lazarev.model.event.OrderPaymentAwaitEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

@Testcontainers
@SpringBootTest
class InventoryServiceTest {

    @Container
    @SuppressWarnings({"resource"})
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres")
            .withDatabaseName("inventory_test_db")
            .withUsername("root")
            .withPassword("root");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dymDynamicPropertyRegistry) {
        dymDynamicPropertyRegistry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        dymDynamicPropertyRegistry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        dymDynamicPropertyRegistry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    InventoryServiceImpl inventoryService;

    @Autowired
    InventoryRepository inventoryRepository;

    @MockBean
    KafkaOrderService kafkaOrderService;

    @Captor
    ArgumentCaptor<OrderPaymentAwaitEvent> captor;

    @Test
    void reserveOrder_shouldReturnTrue_ifProductsInStock() {
        OrderInventoryCheckEvent event = createPositiveTestEvent();
        boolean reserveResult = inventoryService.reserveOrder(event);

        verify(kafkaOrderService, only()).sendMessageToOrderPaymentAwaitTopic(captor.capture());
        OrderPaymentAwaitEvent paymentAwaitEvent = captor.getValue();

        assertThat(reserveResult).isTrue();
        assertThat(paymentAwaitEvent.getTotalPrice()).isEqualTo(BigDecimal.valueOf(340).setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void reserveOrder_shouldReturnFalse_ifProductsNotInStock() {
        OrderInventoryCheckEvent event = createNotInStockTestEvent();
        boolean reserveResult = inventoryService.reserveOrder(event);

        assertThat(reserveResult).isFalse();
        verify(kafkaOrderService, only()).sendMessageToRejectedOrderTopic(any());
    }

    @Test
    void reserveOrder_shouldReturnFalse_ifProductsNotFound() {
        OrderInventoryCheckEvent event = createNotFoundTestEvent();
        boolean reserveResult = inventoryService.reserveOrder(event);

        assertThat(reserveResult).isFalse();
        verify(kafkaOrderService, only()).sendMessageToRejectedOrderTopic(any());
    }

    private OrderInventoryCheckEvent createPositiveTestEvent(){
        OrderInventoryCheckEvent event = new OrderInventoryCheckEvent();
        event.setClientId(1);
        event.setOrderId(1);
        event.setOrderStatus(OrderStatus.INVENTORY_CHECK);
        event.setOrderItems(List.of(
                new OrderItemDto("1", 5),
                new OrderItemDto("2", 3)));
        return event;
    }

    private OrderInventoryCheckEvent createNotInStockTestEvent(){
        OrderInventoryCheckEvent event = new OrderInventoryCheckEvent();
        event.setClientId(1);
        event.setOrderId(1);
        event.setOrderStatus(OrderStatus.INVENTORY_CHECK);
        event.setOrderItems(List.of(
                new OrderItemDto("1", 15),
                new OrderItemDto("2", 3),
                new OrderItemDto("3", 11)));
        return event;
    }

    private OrderInventoryCheckEvent createNotFoundTestEvent(){
        OrderInventoryCheckEvent event = new OrderInventoryCheckEvent();
        event.setClientId(1);
        event.setOrderId(1);
        event.setOrderStatus(OrderStatus.INVENTORY_CHECK);
        event.setOrderItems(List.of(
                new OrderItemDto("1", 5),
                new OrderItemDto("10", 3)));
        return event;
    }
}