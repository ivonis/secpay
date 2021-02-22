package com.ivo.secpay.exception;

public class LoginAlreadyUsedException extends PaymentException {

  static final String MSG_FORMAT = "Login name%s is already used";

  public LoginAlreadyUsedException() {
    super(String.format(MSG_FORMAT, ""));
  }

  public LoginAlreadyUsedException(String loginName) {
    super(String.format(MSG_FORMAT, " " + loginName));
  }
}
