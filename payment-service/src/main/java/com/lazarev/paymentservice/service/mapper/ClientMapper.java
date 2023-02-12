package com.lazarev.paymentservice.service.mapper;

import com.lazarev.model.dto.payment.BankAccountDto;
import com.lazarev.model.dto.payment.ClientDto;
import com.lazarev.paymentservice.entity.BankAccount;
import com.lazarev.paymentservice.entity.Client;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    ClientDto toClientDto(Client client);

    Client toClient(ClientDto clientDto);

    BankAccount toBankAccount(BankAccountDto bankAccountDto);

    BankAccountDto toBankAccountDto(BankAccount bankAccount);
}
