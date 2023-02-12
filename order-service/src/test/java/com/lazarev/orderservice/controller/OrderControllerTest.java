package com.lazarev.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lazarev.model.dto.order.OrderPlaceRequest;
import com.lazarev.model.dto.order.OrderItemDto;
import com.lazarev.model.enums.OrderStatus;
import com.lazarev.model.event.OrderInventoryCheckEvent;
import com.lazarev.orderservice.entity.Order;
import com.lazarev.orderservice.entity.OrderItem;
import com.lazarev.orderservice.service.impl.KafkaOrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Container
    @SuppressWarnings({"resource"})
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres")
            .withDatabaseName("order_test_db")
            .withUsername("root")
            .withPassword("root");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dymDynamicPropertyRegistry) {
        dymDynamicPropertyRegistry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        dymDynamicPropertyRegistry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        dymDynamicPropertyRegistry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    KafkaOrderService kafkaOrderService;

    @Captor
    ArgumentCaptor<OrderInventoryCheckEvent> captor;

    @Autowired
    EntityManager entityManager;

    @Autowired
    PlatformTransactionManager platformTransactionManager;

    @BeforeEach
    void init() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
        transactionTemplate.execute(s -> {
                    List<Order> orders = List.of(createOrder(1), createOrder(1));
                    for(Order order : orders){
                        entityManager.persist(order);
                    }
                    return null;
                }
        );
    }

    @AfterEach
    void clear() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
        transactionTemplate.execute(s ->
                entityManager.createNativeQuery("truncate table orders restart identity cascade").executeUpdate()
        );
    }

    @Test
    void placeNewOrder_shouldReturn201() throws Exception {
        OrderPlaceRequest orderDto = createOrderDto();

        mockMvc.perform(post("/api/orders")
                        .with(jwtWithAdminRole())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isCreated());

        verify(kafkaOrderService, only()).sendMessageToInventoryCheckTopic(captor.capture());
        OrderInventoryCheckEvent inventoryCheckEvent = captor.getValue();
        assertThat(inventoryCheckEvent.getOrderId()).isNotNull();
    }

    @Test
    void getAllOrdersByClientId_shouldReturnList_ifOrdersWereFound() throws Exception {
        Integer clientId = 1;

        mockMvc.perform(get("/api/orders/client/{clientId}", clientId)
                        .with(jwtWithAdminRole()))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].orderItems.length()").value(3))
                .andExpect(status().isOk());
    }

    @Test
    void getAllOrdersByClientId_shouldReturnEmptyList_ifOrdersWerentFound() throws Exception {
        Integer clientId = 10;

        mockMvc.perform(get("/api/orders/client/{clientId}", clientId)
                        .with(jwtWithAdminRole()))
                .andExpect(jsonPath("$.length()").value(0))
                .andExpect(jsonPath("$[0].id").doesNotExist())
                .andExpect(status().isOk());
    }

    @Test
    void getOrderById_shouldReturnOrderResponse_ifOrderWasFound() throws Exception {
        Integer orderId = 1;

        mockMvc.perform(get("/api/orders/{orderId}", orderId)
                        .with(jwtWithAdminRole()))
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.status").value(OrderStatus.NEW.name()))
                .andExpect(jsonPath("$.orderItems").isNotEmpty())
                .andExpect(status().isOk());
    }

    @Test
    void getOrderById_shouldReturnNotFoundError_ifOrderWasntFound() throws Exception {
        Integer orderId = 10;

        mockMvc.perform(get("/api/orders/{orderId}", orderId)
                        .with(jwtWithAdminRole()))
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Order with id='%d' not found".formatted(orderId)))
                .andExpect(status().isNotFound());
    }

    private OrderPlaceRequest createOrderDto() {
        return new OrderPlaceRequest(1, List.of(
                new OrderItemDto("1", 5),
                new OrderItemDto("2", 3)));
    }

    private Order createOrder(Integer clientId) {
        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.NEW);
        order.setClientId(clientId);

        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<OrderItem> orderItems = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            OrderItem orderItem = new OrderItem();
            orderItem.setAmount(random.nextInt(5, 20));
            orderItem.setProductCode(String.valueOf(i + 1));
            orderItems.add(orderItem);
        }
        order.setOrderItems(orderItems);
        return order;
    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtWithAdminRole(){
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtWithUserRole(){
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }
}