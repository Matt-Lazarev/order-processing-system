package com.lazarev.paymentservice.repository;

import com.lazarev.paymentservice.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, Integer> {
}
