package com.ivo.secpay.dao;

import com.ivo.secpay.model.Customer;

public interface CustomerDAO {

  boolean insert(Customer customer);

  Customer find(String login);

  Customer findByToken(String token);

  String setToken(String login, String token);

  void clearToken(String login);

  int incLoginAttempts(String login);

  int getLoginAttempts(String login);

  void clearLoginAttempts(String login);

  void blocking(String login);

  int setBalance(String login, int value);
}
