package com.ivo.secpay.model;

public interface Payment {
  String login(String login, String path);
  void logout();
  void payment();
}
