# order-processing-system

<h3 align="justify">Order Processing App, allows to make orders and controll their status in real-time. </h3>

<hr>
<h3><b>Microservices architecture:</b></h3>
<p align="center">
  <img width="800" alt="image" src="https://user-images.githubusercontent.com/70879589/218324754-b92d7e41-9fbc-4a22-8131-7753bc654520.png">
</p>

<hr>
<h3><b>Business logic:</b></h3>
<ol>
  <li>New order</li>
  <li>Inventory check</li>
  <li>Payment</li>
  <li>Accepted order</li>
</ol>
<p><b>Order Status Flow:</b></b>
<p align="center">
  <img width="637" alt="image" src="https://user-images.githubusercontent.com/70879589/218325472-d17a7992-328a-4830-8a5f-90861ca8202e.png">
</p>
<br>
<p><b>Request Flow:</b></b>
<p align="center">
  <img width="766" alt="image" src="https://user-images.githubusercontent.com/70879589/218326215-e26291e6-2849-4136-a625-e89d85d0c78c.png">
</p>

<hr>
<h3><b>Application includes:</b></h3>
<ul>
  <li>Spring Boot (Multi-module Maven project)</li>
  <li>Spring Data JPA (Hibernate, Postgres)</li>
  <li>Spring Cloud (Netflix Eureka, API Gateway, OpenFeing, Sleuth & Zipkin)</li>
  <li>Spring Kafka Integration (Event-Sourcing)</li>
  <li>OAuth2 & Keycloak (Authorization Code & Client Credentials Flows)</li>
  <li>Spring Boot Test (JUnit, Mockito, Testcontainers)</li>
  <li>Docker (Docker-compose, JIB-plugin)</li>
  <li>Jenkins (CI/CD Pipeline)</li>
  <li>Migrations (Flyway)</li>
  <li>Tools (Lombok, Mapstruct)</li>
</ul>
