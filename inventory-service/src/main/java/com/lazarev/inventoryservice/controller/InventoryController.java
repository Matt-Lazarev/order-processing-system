package com.lazarev.inventoryservice.controller;

import com.lazarev.inventoryservice.service.impl.InventoryServiceImpl;
import com.lazarev.model.dto.inventory.ProductDto;
import com.lazarev.model.dto.inventory.ProductShipmentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryServiceImpl inventoryService;

    @GetMapping("/{productCode}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<ProductDto> getProductByCode(@PathVariable String productCode){
        return ResponseEntity.ok(inventoryService.getProductByCode(productCode));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> saveProduct(@RequestBody ProductDto productDto){
        inventoryService.saveProduct(productDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/add/{productCode}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> addProductInventory(@RequestBody ProductShipmentRequest request){
        inventoryService.addProductInventory(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/subtract/{productCode}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> subtractProductInventory(@RequestBody ProductShipmentRequest request){
        inventoryService.subtractProductInventory(request);
        return ResponseEntity.ok().build();
    }
}
