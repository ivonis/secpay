package com.ivo.secpay.service.impl;

import com.ivo.secpay.Const;
import com.ivo.secpay.Utils;
import com.ivo.secpay.dao.CustomerDAO;
import com.ivo.secpay.exception.CustomerIsBlockedException;
import com.ivo.secpay.exception.InsufficientFundsException;
import com.ivo.secpay.exception.InvalidAuthTokenException;
import com.ivo.secpay.exception.LoginAlreadyUsedException;
import com.ivo.secpay.exception.WrongLoginOrPasswordException;
import com.ivo.secpay.model.Customer;
import com.ivo.secpay.service.CustomerService;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service("customerService")
public class CustomerServiceImpl implements CustomerService {

  @Autowired
  CustomerDAO dao;

  @Autowired
  PasswordEncoder encoder;

  @Override
  public String register(String login, String password) {
    Customer customer = dao.find(login);
    if (customer == null) {
      customer = new Customer(login, encoder.encode(password));
      customer.setBalance(8 * 100);// = $8
      String token = genToken();
      customer.setToken(token);
      if (dao.insert(customer)) {
        return token;
      }
    }
    throw new LoginAlreadyUsedException(login);
  }

  @Override
  public String login(String login, String password) {
    Customer customer = dao.find(login);
    if (customer != null) {
      if (customer.isBlocked()) {
        throw new CustomerIsBlockedException(login);
      }
      if (encoder.matches(password, customer.getPassword())) {
        dao.clearLoginAttempts(login);
        String token = customer.getToken();
        if (token == null) {
          token = dao.setToken(login, genToken());
        }
        return token;
      }
      if (dao.incLoginAttempts(login) >= Const.MAX_LOGIN_ATTEMPTS) {
        dao.blocking(login);
        throw new CustomerIsBlockedException(login);
      }
    }
    throw new WrongLoginOrPasswordException(login);
  }

  @Override
  public Customer findByToken(String token) {
    return dao.findByToken(token);
  }

  @Override
  public void logout(String login) {
    dao.clearToken(login);
  }

  @Override
  public void logoutByToken(String token) {
    Customer customer = dao.findByToken(token);
    if (customer == null) {
      throw new InvalidAuthTokenException(token);
    }
    logout(customer.getLogin());
  }

  @Override
  public BigDecimal payment(String token) {
    Customer customer;
    if (token == null || (customer = dao.findByToken(token)) == null) {
      throw new InvalidAuthTokenException(token);
    }
    if (customer.isBlocked()) {
      throw new CustomerIsBlockedException(customer.getLogin());
    }
    int sum = 110;// = $1.1
    if (customer.getBalance() < sum) {
      throw new InsufficientFundsException(Utils.convertToMoney(customer.getBalance()));
    }
    int balance = dao.setBalance(customer.getLogin(), customer.getBalance() - sum);
    return Utils.convertToMoney(balance);
  }

  protected String genToken() {
    return UUID.randomUUID().toString();
  }
}
