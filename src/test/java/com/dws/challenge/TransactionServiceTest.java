package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.dto.FundTransferDto;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.service.NotificationService;
import com.dws.challenge.service.TransactionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private AccountsRepository accountsRepository;

    @BeforeAll
    public void init(){
        Account firstAccount = new Account("DB101",new BigDecimal(1000));
        Account secondAccount = new Account("DB102",new BigDecimal(500));
        accountsRepository.createAccount(firstAccount);
        accountsRepository.createAccount(secondAccount);
    }

    @Test
    void testTransferFundAccount(){
       // Mockito.doNothing().when(notificationService).notifyAboutTransfer(Mockito.any(Account.class), Mockito.any(String.class));
        Mockito.doNothing().when(notificationService).notifyAboutTransfer(Mockito.any(), Mockito.any(String.class));
        FundTransferDto fundTransferDto = new FundTransferDto("DB101","DB102",new BigDecimal(300));
        transactionService.transferFund(fundTransferDto);
        Account accountFrom = accountsRepository.getAccount("DB101");
        Account accountTo = accountsRepository.getAccount("DB102");
        Assertions.assertTrue(accountFrom.getBalance().equals(new BigDecimal("700"))&&(accountTo.getBalance().equals(new BigDecimal("800"))));
    }
    @Test
    void testTransferFundFailsOnInsufficientBalance(){

        FundTransferDto fundTransferDto = new FundTransferDto("DB101","DB102",new BigDecimal(1500));
        try {
            transactionService.transferFund(fundTransferDto);
        }catch(InsufficientBalanceException ex){
            assertThat(ex.getMessage()).isEqualTo("Insufficient Balance");
        }
    }

    @Test
    void testTransferFundAccountFailOnAccountNotFound(){
        FundTransferDto fundTransferDto = new FundTransferDto("DB103","DB102",new BigDecimal(100));
        try {
            transactionService.transferFund(fundTransferDto);
        }catch(AccountNotFoundException ex){
            assertThat(ex.getMessage()).isEqualTo("Account with account number DB103 not found");
        }
    }
}
