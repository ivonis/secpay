package com.ivo.secpay.exception;

public class InvalidAuthTokenException extends PaymentException {
  static final String MSG = "Invalid auth token:";

  public InvalidAuthTokenException(String message) {
    super(MSG + message);
  }
}
