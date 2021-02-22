package com.ivo.secpay.dao.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.ivo.secpay.dao.CustomerDAO;
import com.ivo.secpay.model.Customer;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

class JdbcCustomerDAOTest {

  @Test
  public  void testAll() {
    DataSource dataSource = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2)
        .addScript("classpath:jdbc/schema.sql")
        .build();
    JdbcCustomerDAO customerDAO = new JdbcCustomerDAO();
    customerDAO.setDataSource(dataSource);
    Customer customer = new Customer("vasia", "12345");
    customer.setBalance(800);
    assertTrue(customerDAO.insert(customer));
    Customer customer1 = customerDAO.find("vasia");
    assertEquals(customer.getLogin(), customer1.getLogin());
    assertEquals(customer.getPassword(), customer1.getPassword());
    assertEquals(customer.getBalance(), customer1.getBalance());
    assertEquals(customer.getToken(), customer1.getToken());

    String token = customerDAO.setToken("vasia", "vasia-ok");
    assertEquals("vasia-ok", token);
    customer = customerDAO.find("vasia");
    assertEquals("vasia-ok", customer.getToken());
    customer1 = customerDAO.findByToken("vasia-ok");
    assertEquals(customer.getLogin(), customer1.getLogin());
    customerDAO.clearToken("vasia");
    assertNull(customerDAO.findByToken("vasia-ok"));
    assertNotNull(customerDAO.find("vasia"));

    assertEquals(1000, customerDAO.setBalance("vasia",1000));
    customer = customerDAO.find("vasia");
    assertEquals(1000, customer.getBalance());
    assertEquals(1, customerDAO.incLoginAttempts("vasia"));
    assertEquals(2, customerDAO.incLoginAttempts("vasia"));
    customerDAO.clearLoginAttempts("vasia");
    assertEquals(1, customerDAO.incLoginAttempts("vasia"));
    customerDAO.blocking("vasia");
    customer = customerDAO.find("vasia");
    assertTrue(customer.isBlocked());
  }

}