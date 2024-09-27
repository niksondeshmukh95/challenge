package com.dws.challenge.web;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.dto.FundTransferDto;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.math.BigDecimal;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

  private final AccountsService accountsService;
  private final TransactionService transactionService;

  @Autowired
  public AccountsController(AccountsService accountsService, TransactionService transactionService) {
    this.accountsService = accountsService;
    this.transactionService = transactionService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
    log.info("Creating account {}", account);

    try {
    this.accountsService.createAccount(account);
    } catch (DuplicateAccountIdException daie) {
      return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(path = "/{accountId}")
  public Account getAccount(@PathVariable String accountId) {
    log.info("Retrieving account for id {}", accountId);
    return this.accountsService.getAccount(accountId);
  }

  @PostMapping(path = "/fund-transfer",consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> transferFund(@RequestBody @Valid FundTransferDto fundTransferDto) {
    log.info("Fund transfer request {}", fundTransferDto);
    if(fundTransferDto.getAmount().compareTo(new BigDecimal(0.0)) <= 0){
      return new ResponseEntity<>("Amount must be positive and can not be zero",HttpStatus.BAD_REQUEST);
    }
    if(fundTransferDto.getAccountFromId().equals(fundTransferDto.getAccountToId())){
      return new ResponseEntity<>("Can not be transfer to same account",HttpStatus.BAD_REQUEST);
    }
    try {
      this.transactionService.transferFund(fundTransferDto);
    } catch (AccountNotFoundException accountNotFoundException) {
      return new ResponseEntity<>(accountNotFoundException.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (InsufficientBalanceException insufficientBalanceException) {
      return new ResponseEntity<>(insufficientBalanceException.getMessage(), HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>("Successfully transferred",HttpStatus.OK);
  }

}
