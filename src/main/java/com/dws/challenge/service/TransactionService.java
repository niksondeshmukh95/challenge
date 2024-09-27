package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.dto.FundTransferDto;
import com.dws.challenge.repository.AccountsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class TransactionService {

    private final AccountsRepository accountsRepository;

    private final NotificationService notificationService;

    @Autowired
    public TransactionService(AccountsRepository accountsRepository, NotificationService notificationService) {
        this.accountsRepository = accountsRepository;
        this.notificationService = notificationService;
    }

    public synchronized void transferFund(FundTransferDto fundTransferDto){
        Account accountFrom = accountsRepository.getAccount(fundTransferDto.getAccountFromId());
        Account accountTo = accountsRepository.getAccount(fundTransferDto.getAccountToId());
        if(accountFrom == null) {
            throw new AccountNotFoundException("Account with account number " + fundTransferDto.getAccountFromId() + " not found");
        }
        if(accountTo == null) {
            throw new AccountNotFoundException("Account with account number " + fundTransferDto.getAccountToId() + " not found");
        }
        if(accountFrom.getBalance().compareTo(fundTransferDto.getAmount()) < 0){
            throw new InsufficientBalanceException("Insufficient Balance");
        }
        accountsRepository.updateAccountBalance(accountFrom, accountFrom.getBalance().subtract(fundTransferDto.getAmount()));
        accountsRepository.updateAccountBalance(accountTo, accountTo.getBalance().add(fundTransferDto.getAmount()));
        String pattern = "dd-MM-yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        String date = simpleDateFormat.format(new Date());
        //Sending notification to both accounts
        notificationService.notifyAboutTransfer(accountFrom, "Your a/c no."+accountFrom.getAccountId()+" is debited with INR "+ fundTransferDto.getAmount() +"and transfer to a/c no."+ accountTo.getAccountId() +" on "+ date +", available balance is INR "+ accountFrom.getBalance());
        notificationService.notifyAboutTransfer(accountTo,"Your a/c no."+accountTo.getAccountId()+" is credited with INR "+ fundTransferDto.getAmount() +"and transfer from a/c no."+ accountFrom.getAccountId()+" on "+ date +", available balance is INR "+ accountTo.getBalance());
    }

}
