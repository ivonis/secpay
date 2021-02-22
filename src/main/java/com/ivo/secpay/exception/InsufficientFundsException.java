package com.ivo.secpay.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends PaymentException {
  static final String MSG = "Insufficient funds";

  public InsufficientFundsException(BigDecimal balance) {
    super(MSG + ":" + balance);
  }
}
