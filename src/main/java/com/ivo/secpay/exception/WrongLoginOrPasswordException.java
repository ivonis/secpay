package com.ivo.secpay.exception;

public class WrongLoginOrPasswordException extends PaymentException {
  static final String MSG = "Wrong login or password";

  public WrongLoginOrPasswordException() {
    super(MSG);
  }

  public WrongLoginOrPasswordException(String message) {
    super(MSG + ": " + message);
  }

  public WrongLoginOrPasswordException(String message, Throwable cause) {
    super(message, cause);
  }

  public WrongLoginOrPasswordException(Throwable cause) {
    super(cause);
  }
}
