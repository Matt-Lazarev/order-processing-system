package com.lazarev.inventoryservice.service.impl;

import com.lazarev.inventoryservice.entity.Product;
import com.lazarev.inventoryservice.exception.ProductNotFoundException;
import com.lazarev.inventoryservice.repository.InventoryRepository;
import com.lazarev.inventoryservice.service.abst.InventoryService;
import com.lazarev.inventoryservice.service.mapper.ProductMapper;
import com.lazarev.model.dto.order.OrderItemDto;
import com.lazarev.model.dto.inventory.ProductDto;
import com.lazarev.model.dto.inventory.ProductShipmentRequest;
import com.lazarev.model.enums.OrderStatus;
import com.lazarev.model.event.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service

@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final KafkaOrderService kafkaOrderService;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductByCode(String productCode) {
         Product product = inventoryRepository.findProductByCode(productCode)
                 .orElseThrow(()->new ProductNotFoundException("Product with code='%s' not found".formatted(productCode)));

         return productMapper.toProductDto(product);
    }

    @Override
    @Transactional
    public void addProductInventory(ProductShipmentRequest request) {
        Product product = inventoryRepository.findProductByCode(request.productCode())
                .orElseThrow(()->new ProductNotFoundException("Product with code='%s' not found".formatted(request.productCode())));

        product.setAmount(product.getAmount() + request.amount());
        inventoryRepository.save(product);
    }

    @Override
    @Transactional
    public void subtractProductInventory(ProductShipmentRequest request) {
        Product product = inventoryRepository.findProductByCode(request.productCode())
                .orElseThrow(()->new ProductNotFoundException("Product with code='%s' not found".formatted(request.productCode())));

        if(product.getAmount() < request.amount()){
            throw new IllegalArgumentException("Not enough amount of products in inventory");
        }

        product.setAmount(product.getAmount() - request.amount());
        inventoryRepository.save(product);
    }

    @Override
    @Transactional
    public void saveProduct(ProductDto productDto) {
        Product product = productMapper.toProduct(productDto);
        inventoryRepository.save(product);
    }

    @Override
    @Transactional
    public boolean reserveOrder(OrderInventoryCheckEvent event) {
        boolean checkResult = reserveProducts(event);
        if(!checkResult){
            OrderRejectEvent rejectedEvent = new OrderRejectEvent(event.getOrderId(), event.getClientId(), OrderStatus.REJECTED, "Not all products are in stock");
            kafkaOrderService.sendMessageToRejectedOrderTopic(rejectedEvent);
            return false;
        }

        BigDecimal totalPrice = calculateOrderTotalPrice(event);
        OrderPaymentAwaitEvent inventoryCheckedEvent = new OrderPaymentAwaitEvent(
                event.getOrderId(), event.getClientId(), OrderStatus.PAYMENT_AWAIT, totalPrice);

        kafkaOrderService.sendMessageToOrderPaymentAwaitTopic(inventoryCheckedEvent);
        return true;
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

    private boolean reserveProducts(OrderInventoryCheckEvent event){
        Map<String, OrderItemDto> orders = groupByProductCode(event.getOrderItems());
        List<Product> products = inventoryRepository.findAllByCodeIn(orders.keySet());

        boolean allProductsFound = orders.size() == products.size();
        boolean allProductsInStock = products.stream()
                .allMatch(p -> p.getAmount() > orders.get(p.getCode()).amount());

        if(!allProductsFound || !allProductsInStock){
            return false;
        }

        products.forEach(p -> inventoryRepository.subtractProductAmount(p.getCode(), orders.get(p.getCode()).amount()));
        return true;
    }

    private BigDecimal calculateOrderTotalPrice(OrderInventoryCheckEvent event){
        Map<String, OrderItemDto> orders = groupByProductCode(event.getOrderItems());
        List<Product> products = inventoryRepository.findAllByCodeIn(orders.keySet());

        return products
                .stream()
                .map(p -> BigDecimal.valueOf(orders.get(p.getCode()).amount()).multiply(p.getPrice()))
                .reduce(new BigDecimal(0).setScale(2, RoundingMode.HALF_UP), BigDecimal::add);
    }

    private Map<String, OrderItemDto> groupByProductCode(List<OrderItemDto> orderItems){
        return orderItems
                .stream()
                .collect(Collectors.toMap(OrderItemDto::productCode, Function.identity()));
    }
}
