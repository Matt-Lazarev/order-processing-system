package com.lazarev.orderservice.controller;

import com.lazarev.model.dto.order.OrderPlaceRequest;
import com.lazarev.model.dto.order.OrderResponse;
import com.lazarev.orderservice.service.impl.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
public class OrderController {
    private final OrderServiceImpl orderService;

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<OrderResponse>> getAllOrdersByClientId(@PathVariable Integer clientId){
        return ResponseEntity.ok(orderService.getAllOrdersByClientId(clientId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Integer orderId){
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @PostMapping
    public ResponseEntity<?> placeNewOrder(@RequestBody OrderPlaceRequest order){
        orderService.placeNewOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
