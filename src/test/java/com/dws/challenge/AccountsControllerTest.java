package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @MockBean
  private NotificationService notificationService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @BeforeEach
  void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();
    Mockito.doNothing().when(notificationService).notifyAboutTransfer(Mockito.any(Account.class), Mockito.any(String.class));
    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

  @Test
  void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
      .andExpect(status().isOk())
      .andExpect(
        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
  }

  @Test
  void fundTransferEmptyFromAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/fund-transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountFromId\":\"\",\"accountToId\":\"DB002\",\"amount\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void fundTransferEmptyAmount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/fund-transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountFromId\":\"DB001\",\"accountToId\":\"DB002\",\"amount\":\"\"}")).andExpect(status().isBadRequest());
  }

  @Test
  void fundTransferEmptyToAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/fund-transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountFromId\":\"DB001\",\"accountToId\":\"\",\"amount\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void fundTransferWithNegativeAmount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/fund-transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountFromId\":\"DB001\",\"accountToId\":\"DB002\",\"amount\":-100}")).andExpect(status().isBadRequest())
            .andExpect(content().string("Amount must be positive and can not be zero"));
  }

  @Test
  void fundTransferWithZeroAmount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/fund-transfer").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"accountFromId\":\"DB001\",\"accountToId\":\"DB002\",\"amount\":0}")).andExpect(status().isBadRequest())
            .andExpect(content().string("Amount must be positive and can not be zero"));
  }

  @Test
  void fundTransferWithSameAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts/fund-transfer").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"accountFromId\":\"DB002\",\"accountToId\":\"DB002\",\"amount\":100}")).andExpect(status().isBadRequest())
            .andExpect(content().string("Can not be transfer to same account"));
  }

  @Test
  void fundTransfer() throws Exception {

    Account firstAccount = new Account("DB001", new BigDecimal("1000"));
    Account secondAccount = new Account("DB002", new BigDecimal("0"));
    this.accountsService.createAccount(firstAccount);
    this.accountsService.createAccount(secondAccount);

    this.mockMvc.perform(post("/v1/accounts/fund-transfer").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"accountFromId\":\"DB001\",\"accountToId\":\"DB002\",\"amount\":100}"))
            .andExpect(status().isOk());
  }
}
