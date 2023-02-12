package com.lazarev.inventoryservice.service.abst;

import com.lazarev.model.dto.inventory.ProductDto;
import com.lazarev.model.dto.inventory.ProductShipmentRequest;
import com.lazarev.model.enums.OrderStatus;
import com.lazarev.model.event.OrderInventoryCheckEvent;

public interface InventoryService {
    ProductDto getProductByCode(String productCode);

    void addProductInventory(ProductShipmentRequest request);

    void subtractProductInventory(ProductShipmentRequest request);

    void saveProduct(ProductDto productDto);

    boolean reserveOrder(OrderInventoryCheckEvent event);

    void sendUpdatedOrderEvent(Integer orderId, Integer clientId, String message, OrderStatus newStatus);

    void sendRejectedOrderEvent(Integer orderId, Integer clientId, String reason);
}
