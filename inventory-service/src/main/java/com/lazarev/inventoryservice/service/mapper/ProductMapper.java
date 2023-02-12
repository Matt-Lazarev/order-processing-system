package com.lazarev.inventoryservice.service.mapper;

import com.lazarev.inventoryservice.entity.Product;
import com.lazarev.model.dto.inventory.ProductDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductDto toProductDto(Product product);
    Product toProduct(ProductDto productDto);
}
