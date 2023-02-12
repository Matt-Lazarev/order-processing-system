package com.lazarev.orderservice.repository;

import com.lazarev.model.enums.OrderStatus;
import com.lazarev.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("""
        select distinct o from Order o
        left join fetch o.orderItems
        where o.clientId = :clientId
    """)
    List<Order> findAllOrdersByClientId(Integer clientId);

    @Query("""
        select o from Order o
        left join fetch o.orderItems
        where o.id = :id
    """)
    Optional<Order> findOrderById(Integer id);

    @Modifying
    @Query("update Order o set o.status = :status where o.id = :id")
    void updateOrderStatus(Integer id, OrderStatus status);

    @Modifying
    @Query("update Order o set o.status = :status, o.message = :reason where o.id = :id")
    void updateOrderStatusAndMessage(Integer id, OrderStatus status, String reason);



}
