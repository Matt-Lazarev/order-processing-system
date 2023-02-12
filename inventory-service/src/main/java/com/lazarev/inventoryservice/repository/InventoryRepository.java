package com.lazarev.inventoryservice.repository;

import com.lazarev.inventoryservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface InventoryRepository extends JpaRepository<Product, Integer> {

    @Query("select p from Product p where p.code in :codes")
    List<Product> findAllByCodeIn(Set<String> codes);

    @Query("select p from Product p where p.code = :code")
    Optional<Product> findProductByCode(String code);

    @Modifying
    @Query("update Product p set p.amount = p.amount - :amount where p.code = :code")
    void subtractProductAmount(String code, int amount);
}
