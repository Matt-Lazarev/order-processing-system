package com.lazarev.paymentservice.controller;

import com.lazarev.model.dto.payment.BankAccountDto;
import com.lazarev.model.dto.payment.ClientDto;
import com.lazarev.model.dto.payment.PaymentReserveResult;
import com.lazarev.paymentservice.service.abst.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clients")
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
public class ClientController {
    private final ClientService clientService;

    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> getClientById(@PathVariable Integer id){
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    @GetMapping("/{clientId}/balance-check")
    public ResponseEntity<PaymentReserveResult> checkPaymentPossibility(@PathVariable Integer clientId,
                                                        @RequestParam BigDecimal paymentAmount){
        return ResponseEntity.ok(clientService.checkPaymentPossibility(clientId, paymentAmount));
    }

    @PostMapping
    public ResponseEntity<?> saveClient(@RequestBody ClientDto clientDto){
        clientService.saveClient(clientDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{clientId}/attach-bank-account")
    public ResponseEntity<?> attachBankAccount(@PathVariable Integer clientId,
                                               @RequestBody BankAccountDto bankAccountDto){
        clientService.attachBankAccount(clientId, bankAccountDto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{clientId}/detach-bank-account")
    public ResponseEntity<?> detachBankAccount(@PathVariable Integer clientId){
        clientService.detachBankAccount(clientId);
        return ResponseEntity.ok().build();
    }
}
