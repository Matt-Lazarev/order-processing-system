package com.lazarev.paymentservice.repository;

import com.lazarev.paymentservice.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Integer> {

    @Query("""
       select c from Client c
       left join fetch c.bankAccount
       where c.id = :id
    """)
    Optional<Client> findClientById(Integer id);
}
