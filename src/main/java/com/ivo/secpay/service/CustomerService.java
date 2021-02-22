package com.ivo.secpay.service;

import com.ivo.secpay.model.Customer;
import java.math.BigDecimal;

public interface CustomerService {
  String register(String login, String password);
  String login(String login, String password);
  Customer findByToken(String token);
  void logout(String login);
  void logoutByToken(String token);
  BigDecimal payment(String token);
}
