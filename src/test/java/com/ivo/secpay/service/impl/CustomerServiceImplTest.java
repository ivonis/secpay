package com.ivo.secpay.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ivo.secpay.Const;
import com.ivo.secpay.SecpayApplication;
import com.ivo.secpay.config.SpringJdbcConfig;
import com.ivo.secpay.dao.CustomerDAO;
import com.ivo.secpay.exception.CustomerIsBlockedException;
import com.ivo.secpay.exception.InsufficientFundsException;
import com.ivo.secpay.exception.InvalidAuthTokenException;
import com.ivo.secpay.exception.LoginAlreadyUsedException;
import com.ivo.secpay.exception.WrongLoginOrPasswordException;
import com.ivo.secpay.model.Customer;
import com.ivo.secpay.service.CustomerService;
import java.math.BigDecimal;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {SecpayApplication.class, SpringJdbcConfig.class})
class CustomerServiceImplTest {

  @Autowired
  DataSource dataSource;

  @Autowired
  CustomerDAO customerDAO;

  @Autowired
  CustomerService customerService;

  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() {
    if (jdbcTemplate == null) {
      jdbcTemplate = new JdbcTemplate(dataSource);
    }
    jdbcTemplate.execute("TRUNCATE TABLE customer");
  }

  @Test
  public void test1() {
    assertNotNull(customerDAO);
    assertNotNull(customerService);
    String token = customerService.register("kuzia", "12345");
    assertNotNull(token);
    Customer c1 = customerDAO.find("kuzia");
    assertNotNull(c1);
    assertEquals("kuzia", c1.getLogin());
    Customer c2 = customerDAO.findByToken(token);
    assertNotNull(c2);
    assertEquals("kuzia", c2.getLogin());
    c2 = customerService.findByToken(token);
    assertEquals("kuzia", c2.getLogin());
    assertEquals(token, customerService.login("kuzia", "12345"));
    assertThrows(LoginAlreadyUsedException.class, () -> customerService.register("kuzia", "2222"));
    BigDecimal balance = BigDecimal.valueOf(8);
    BigDecimal debit = BigDecimal.valueOf(1.1);
    balance = balance.subtract(debit);
    assertEquals(balance.doubleValue(), customerService.payment(token).doubleValue());

    assertThrows(WrongLoginOrPasswordException.class, () -> customerService.login("kuzia1", "12345"));
    int i = 1;
    while (i++ < Const.MAX_LOGIN_ATTEMPTS) {
      assertThrows(WrongLoginOrPasswordException.class, () -> customerService.login("kuzia", "failed-pass"));
    }
    assertThrows(CustomerIsBlockedException.class, () -> customerService.login("kuzia", "failed-pass"));
    assertThrows(CustomerIsBlockedException.class, () -> customerService.payment(token));
    assertThrows(InvalidAuthTokenException.class, () -> customerService.payment("failed-token"));
    String token1 = customerService.register("kuzia1", "999");
    customerService.logoutByToken(token1);

    assertThrows(InvalidAuthTokenException.class, () -> customerService.payment(token1));
    String token2 = customerService.login("kuzia1", "999");
    balance = BigDecimal.valueOf(8);
    while (balance.doubleValue() >= debit.doubleValue()) {
      balance = balance.subtract(debit);
      assertEquals(balance.doubleValue(), customerService.payment(token2).doubleValue());
    }
    assertThrows(InsufficientFundsException.class, () -> customerService.payment(token2));
  }

}