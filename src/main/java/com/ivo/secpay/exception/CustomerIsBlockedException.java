package com.ivo.secpay.exception;

public class CustomerIsBlockedException extends PaymentException {

  static final String MSG_FORMAT = "Customer%s is blocked";

  public CustomerIsBlockedException() {
    super(String.format(MSG_FORMAT, ""));
  }

  public CustomerIsBlockedException(String loginName) {
    super(String.format(MSG_FORMAT, " " + loginName));
  }
}
