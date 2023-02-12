package com.lazarev.paymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lazarev.model.dto.payment.BankAccountDto;
import com.lazarev.model.dto.payment.ClientDto;
import com.lazarev.paymentservice.entity.BankAccount;
import com.lazarev.paymentservice.entity.Client;
import com.lazarev.paymentservice.repository.ClientRepository;
import com.lazarev.paymentservice.service.abst.ClientService;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.opaqueToken;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class ClientControllerTest {
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
                entityManager.createNativeQuery("truncate table clients restart identity cascade").executeUpdate()
        );
    }

    @Test
    void getClientById_shouldReturnClient_ifClientExists() throws Exception {
        Integer clientId = 1;

        mockMvc.perform(get("/api/clients/{clientId}", clientId)
                        .with(jwtWithAdminRole()))
                .andExpect(jsonPath("$.firstname").value("Mike"))
                .andExpect(jsonPath("$.lastname").value("Scott"))
                .andExpect(jsonPath("$.email").value("mike@gmail.com"))
                .andExpect(jsonPath("$.bankAccount").isNotEmpty())
                .andExpect(status().isOk());
    }

    @Test
    void getClientById_shouldReturn404_ifClientDoesntExist() throws Exception {
        Integer clientId = 10;

        mockMvc.perform(get("/api/clients/{clientId}", clientId)
                        .with(jwtWithAdminRole()))
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Client with id='%d' not found".formatted(clientId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void checkPaymentPossibility_shouldReturnTrue_ifClientExistsAndEnoughMoneyOnBankAccount() throws Exception{
        Integer clientId = 2;
        String paymentAmount = "1000";

        mockMvc.perform(get("/api/clients/{clientId}/balance-check", clientId)
                        .with(jwtWithAdminRole())
                        .param("paymentAmount", paymentAmount))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Payment is possible"))
                .andExpect(status().isOk());
    }

    @Test
    void checkPaymentPossibility_shouldReturn404_ifClientDoesntExist() throws Exception{
        Integer clientId = 10;
        String paymentAmount = "1000";

        mockMvc.perform(get("/api/clients/{clientId}/balance-check", clientId)
                        .with(jwtWithAdminRole())
                        .param("paymentAmount", paymentAmount))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").value("Client with id='%d' not found".formatted(clientId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void checkPaymentPossibility_shouldReturnFalse_ifBankAccountIsNotAttached() throws Exception{
        String paymentAmount = "1000";
        Integer clientId = saveClientWithoutBankAccount("Bill", "White", "bill@gmail.com");

        mockMvc.perform(get("/api/clients/{clientId}/balance-check", clientId)
                        .with(jwtWithAdminRole())
                        .param("paymentAmount", paymentAmount))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Bank Account is not attached"))
                .andExpect(status().isOk());
    }

    @Test
    void checkPaymentPossibility_shouldReturnFalse_ifNotEnoughMoney() throws Exception{
        Integer clientId = 1;
        String paymentAmount = "1500";

        mockMvc.perform(get("/api/clients/{clientId}/balance-check", clientId)
                        .with(jwtWithAdminRole())
                        .param("paymentAmount", paymentAmount))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Not enough money"))
                .andExpect(status().isOk());
    }

    @Test
    void saveProduct_shouldReturn201() throws Exception {
        ClientDto clientDto = createClientDto();

        mockMvc.perform(post("/api/clients")
                        .with(opaqueToken().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(clientDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void attachBankAccount_shouldReturn200_ifClientExists() throws Exception {
        Integer clientId = 1;
        BankAccountDto bankAccountDto = createBankAccountDto();

        mockMvc.perform(put("/api/clients/{clientId}/attach-bank-account", clientId)
                        .with(jwtWithAdminRole())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(bankAccountDto)))
                .andExpect(status().isOk());

        Client client = clientRepository.findClientById(clientId).get();
        BankAccount newBankAccount = client.getBankAccount();

        assertThat(newBankAccount.getNumber()).isEqualTo(bankAccountDto.number());
        assertThat(newBankAccount.getBalance()).isEqualTo(bankAccountDto.balance());
    }

    @Test
    void attachBankAccount_shouldReturn404_ifClientDoesntExist() throws Exception {
        Integer clientId = 10;
        BankAccountDto bankAccountDto = createBankAccountDto();

        mockMvc.perform(put("/api/clients/{clientId}/attach-bank-account", clientId)
                        .with(jwtWithAdminRole())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(bankAccountDto)))
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Client with id='%d' not found".formatted(clientId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void detachBankAccount_shouldReturn200_ifClientExists() throws Exception {
        Integer clientId = 1;

        Client client = clientRepository.findClientById(clientId).get();
        BankAccount oldBankAccount = client.getBankAccount();

        mockMvc.perform(put("/api/clients/{clientId}/detach-bank-account", clientId)
                        .with(jwtWithAdminRole())
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        client = clientRepository.findClientById(clientId).get();
        BankAccount newBankAccount = client.getBankAccount();

        assertThat(oldBankAccount).isNotNull();
        assertThat(newBankAccount).isNull();
    }

    @Test
    void detachBankAccount_shouldReturn404_ifClientDoesntExist() throws Exception {
        Integer clientId = 10;

        mockMvc.perform(put("/api/clients/{clientId}/detach-bank-account", clientId)
                        .with(jwtWithAdminRole())
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Client with id='%d' not found".formatted(clientId)))
                .andExpect(status().isNotFound());
    }

    private Integer saveClientWithoutBankAccount(String firstname, String lastname, String email){
        Client client = new Client();
        client.setFirstname(firstname);
        client.setFirstname(lastname);
        client.setEmail(email);
        saveClient(client);
        return getClientByEmail(email).getId();
    }

    private void saveClient(Client client){
        TransactionTemplate transactionTemplate = new TransactionTemplate(platformTransactionManager);
        transactionTemplate.execute(s -> {
            entityManager.persist(client);
            return null;
        });
    }

    private Client getClientByEmail(String email){
        TypedQuery<Client> query = entityManager.createQuery("select c from Client c where c.email=:email", Client.class);
        query.setParameter("email", email);
        return query.getSingleResult();
    }

    private ClientDto createClientDto() {
        return new ClientDto("Bill", "White", "bill@gmail.com",
                new BankAccountDto("1000-1000-1000-1000", new BigDecimal("150.00")));
    }

    private BankAccountDto createBankAccountDto(){
        return new BankAccountDto("1000-1000-1000-1000", new BigDecimal("150.00"));
    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtWithAdminRole(){
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtWithUserRole(){
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }
}