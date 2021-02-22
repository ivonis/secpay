package com.ivo.secpay.dao.impl;

import com.ivo.secpay.Utils;
import com.ivo.secpay.dao.CustomerDAO;
import com.ivo.secpay.model.Customer;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED)
public class JdbcCustomerDAO implements CustomerDAO {

  private static final String INSERT_SQL = """
        INSERT INTO customer\
        (`login`, `password`, `token`, `balance`, `login_attempts`, `blocked`)\
         VALUES(?, ?, ?, ?, ?, ?)\
      """;
  private static final String SEL_BY_LOGIN_SQL = """
      SELECT `login`, `password`, `token`, `balance`, `blocked` FROM customer WHERE login = ?
      """;

  private static final String SEL_BY_TOKEN_SQL = """
      SELECT `login`, `password`, `token`, `balance`, `blocked` FROM customer WHERE token = ?
      """;

  private static final String SET_TOKEN_SQL = "UPDATE customer SET token = ? WHERE login = ?";
  private static final String SEL_TOKEN_SQL = "SELECT token FROM customer WHERE login = ? LIMIT 1";

  private static final String INC_LA_SQL = "UPDATE customer SET login_attempts = login_attempts + 1 WHERE login = ?";
  private static final String SEL_LA_SQL = "SELECT login_attempts FROM customer WHERE login = ? LIMIT 1";
  private static final String CLR_LA_SQL = "UPDATE customer SET login_attempts = 0 WHERE login = ?";

  private static final String SET_BLOCKED_SQL = "UPDATE customer SET blocked = 1 WHERE login = ?";

  private static final String SET_BALANCE_SQL = "UPDATE customer SET balance = ? WHERE login = ?";
  private static final String SEL_BALANCE_SQL = "SELECT balance FROM customer WHERE login = ? LIMIT 1";

  private static final RowMapper<Customer> ROW_MAPPER = (rs, rowNum) -> {
    Customer customer = new Customer(rs.getString("login"), rs.getString("password"));
    customer.setToken(rs.getString("token"));
    customer.setBalance(rs.getInt("balance"));
    customer.setBlocked(rs.getInt("blocked") != 0);
    return customer;
  };

  private JdbcTemplate jdbcTemplate;

  @SuppressWarnings("unused")
  @Autowired
  public void setDataSource(DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  @Override
  public boolean insert(Customer customer) {
    try {
      int update = jdbcTemplate.update(
          INSERT_SQL,
          customer.getLogin(),
          customer.getPassword(),
          customer.getToken(),
          customer.getBalance(),
          0, 0);
      return update > 0;
    } catch (DataAccessException e) {
      return false;
    }

  }

  @Override
  public Customer find(String login) {
    try {
      List<Customer> customers = jdbcTemplate.query(SEL_BY_LOGIN_SQL, ROW_MAPPER, login);
      return customers.isEmpty() ? null : customers.get(0);
    } catch (DataAccessException e) {
      return null;
    }
  }

  @Override
  public Customer findByToken(String token) {
    try {
      List<Customer> customers = jdbcTemplate.query(SEL_BY_TOKEN_SQL, ROW_MAPPER, token);
      return customers.isEmpty() ? null : customers.get(0);
    } catch (DataAccessException e) {
      return null;
    }
  }

  @Override
  public String setToken(String login, String token) {
    jdbcTemplate.update(SET_TOKEN_SQL, token, login);
    return jdbcTemplate.query(SEL_TOKEN_SQL, Utils.STRING_EXTRACTOR, login);
  }

  @Override
  public void clearToken(String login) {
    jdbcTemplate.update(SET_TOKEN_SQL, null, login);
  }

  @Override
  public int incLoginAttempts(String login) {
    jdbcTemplate.update(INC_LA_SQL, login);
    return getLoginAttempts(login);
  }

  @Override
  public int getLoginAttempts(String login) {
    return Optional.ofNullable(jdbcTemplate.query(SEL_LA_SQL, Utils.INT_EXTRACTOR, login))
        .orElse(0);
  }

  @Override
  public void clearLoginAttempts(String login) {
    jdbcTemplate.update(CLR_LA_SQL, login);
  }

  @Override
  public void blocking(String login) {
    jdbcTemplate.update(SET_BLOCKED_SQL, login);
  }

  @Override
  public int setBalance(String login, int value) {
    jdbcTemplate.update(SET_BALANCE_SQL, value, login);
    return Optional.ofNullable(jdbcTemplate.query(SEL_BALANCE_SQL, Utils.INT_EXTRACTOR, login))
        .orElse(0);
  }
}
