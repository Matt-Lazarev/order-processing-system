package com.lazarev.inventoryservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lazarev.model.dto.inventory.ProductDto;
import com.lazarev.model.dto.inventory.ProductShipmentRequest;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import javax.persistence.TypedQuery;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class InventoryControllerTest {

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
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    Flyway flyway;

    @Autowired
    EntityManager entityManager;

    @Autowired
    PlatformTransactionManager platformTransactionManager;

    @BeforeEach
    void init() {
        flyway.clean();
        flyway.migrate();
    }

    @AfterEach
    void clear(){
        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
        transactionTemplate.execute(s ->
            entityManager.createQuery("delete from Product").executeUpdate()
        );
    }

    @Test
    //@WithMockUser(username = "Mike", roles = "USER")
    void getProductByCode_shouldReturnProduct_ifProductCodeCorrect() throws Exception {
        String productCode = "1";

        mockMvc.perform(get("/api/inventory/{productCode}", productCode)
                        .with(jwtWithAdminRole()))
                .andExpect(jsonPath("$.name").value("Milk"))
                .andExpect(jsonPath("$.price").value(BigDecimal.valueOf(50.00)))
                .andExpect(jsonPath("$.amount").value(15))
                .andExpect(status().isOk());
    }

    @Test
    void getProductByCode_shouldReturnErrorResponse_ifProductDoesntExist() throws Exception {
        String productCode = "10";

        mockMvc.perform(get("/api/inventory/{productCode}", productCode)
                        .with(jwtWithAdminRole()))
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Product with code='%s' not found".formatted(productCode)))
                .andExpect(status().isNotFound());
    }

    @Test
    void saveProduct_shouldReturn201() throws Exception {
        ProductDto productDto = createProductDto();

        mockMvc.perform(post("/api/inventory")
                        .with(jwtWithAdminRole())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void addProductInventory_shouldReturnOk_ifProductCodeCorrect() throws Exception {
        String productCode = "1";
        int amountToAdd = 10;
        ProductShipmentRequest shipmentRequest = createProductShipmentRequest(productCode, amountToAdd);

        int amountBeforeAdd = getProductAmountByCodeQuery(productCode);

        mockMvc.perform(put("/api/inventory/add/{productCode}", productCode)
                        .with(jwtWithAdminRole())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(shipmentRequest)))
                .andExpect(status().isOk());

        int amountAfterAdd = getProductAmountByCodeQuery(productCode);
        assertThat(amountAfterAdd-amountBeforeAdd).isEqualTo(amountToAdd);
    }

    @Test
    void addProductInventory_shouldReturnNotFound_ifProductDoesntExist() throws Exception {
        String productCode = "10";
        int amountToAdd = 10;
        ProductShipmentRequest shipmentRequest = createProductShipmentRequest(productCode, amountToAdd);

        mockMvc.perform(put("/api/inventory/add/{productCode}", productCode)
                        .with(jwtWithAdminRole())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(shipmentRequest)))
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Product with code='%s' not found".formatted(productCode)))
                .andExpect(status().isNotFound());
    }

    @Test
    void subtractProductInventory_shouldReturnOk_ifProductCodeCorrect() throws Exception {
        String productCode = "1";
        int amountToSubtract = 10;
        ProductShipmentRequest shipmentRequest = createProductShipmentRequest(productCode, amountToSubtract);

        int amountBeforeAdd = getProductAmountByCodeQuery(productCode);

        mockMvc.perform(put("/api/inventory/subtract/{productCode}", productCode)
                        .with(jwtWithAdminRole())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(shipmentRequest)))
                .andExpect(status().isOk());

        int amountAfterAdd = getProductAmountByCodeQuery(productCode);
        assertThat(amountBeforeAdd-amountAfterAdd).isEqualTo(amountToSubtract);
    }

    @Test
    void subtractProductInventory_shouldReturnNotFound_ifProductDoesntExist() throws Exception {
        String productCode = "10";
        int amountToAdd = 10;
        ProductShipmentRequest shipmentRequest = createProductShipmentRequest(productCode, amountToAdd);

        mockMvc.perform(put("/api/inventory/subtract/{productCode}", productCode)
                        .with(jwtWithAdminRole())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(shipmentRequest)))
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Product with code='%s' not found".formatted(productCode)))
                .andExpect(status().isNotFound());
    }

    @Test
    void subtractProductInventory_shouldReturnBadRequest_ifProductAmountIsNotEnough() throws Exception {
        String productCode = "1";
        int amountToAdd = 20;
        ProductShipmentRequest shipmentRequest = createProductShipmentRequest(productCode, amountToAdd);

        mockMvc.perform(put("/api/inventory/subtract/{productCode}", productCode)
                        .with(jwtWithAdminRole())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(shipmentRequest)))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Not enough amount of products in inventory"))
                .andExpect(status().isBadRequest());
    }

    private ProductDto createProductDto(){
        return new ProductDto("6", "Salad", BigDecimal.valueOf(75), 15);
    }

    private ProductShipmentRequest createProductShipmentRequest(String productCode, Integer amount){
        return new ProductShipmentRequest(productCode, amount);
    }

    private Integer getProductAmountByCodeQuery(String code){
        TypedQuery<Integer> query = entityManager.createQuery("select p.amount from Product p where p.code = :code", Integer.class);
        query.setParameter("code", code);
        return query.getSingleResult();
    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtWithAdminRole(){
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtWithUserRole(){
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }
}