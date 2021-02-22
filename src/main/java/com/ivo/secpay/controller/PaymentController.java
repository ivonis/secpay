package com.ivo.secpay.controller;

import com.ivo.secpay.Const;
import com.ivo.secpay.Utils;
import com.ivo.secpay.exception.CustomerIsBlockedException;
import com.ivo.secpay.exception.InsufficientFundsException;
import com.ivo.secpay.exception.InvalidAuthTokenException;
import com.ivo.secpay.exception.LoginAlreadyUsedException;
import com.ivo.secpay.exception.WrongLoginOrPasswordException;
import com.ivo.secpay.service.CustomerService;
import java.math.BigDecimal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class PaymentController {

  @Autowired
  CustomerService customerService;

  @GetMapping("/")
  public String hello() {
    return "hello";
  }

  @PostMapping("/register")
  public//@ResponseBody
  ResponseEntity<String> register(
      @RequestParam("login") String login,
      @RequestParam("password") String password,
      HttpSession session,
      HttpServletResponse response) {
    try {
      String token = customerService.register(login, password);
      session.setAttribute(Const.TOKEN_ATTR, token);
      Utils.setTokenToResponse(response, token);
      return ResponseEntity.status(HttpStatus.CREATED).body(token);
    } catch (LoginAlreadyUsedException e) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
    } catch (Throwable t) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, t.getMessage(), t);
    }
  }

  @PostMapping("/login")
  public @ResponseBody
  ResponseEntity<String> login(
      @RequestParam("login") String login,
      @RequestParam("password") String password,
      HttpSession session,
      HttpServletResponse response) {
    try {
      String token = customerService.login(login, password);
      session.setAttribute(Const.TOKEN_ATTR, token);
      Utils.setTokenToResponse(response, token);
      return ResponseEntity.status(HttpStatus.OK).body(token);
    } catch (CustomerIsBlockedException e) {
      throw new ResponseStatusException(HttpStatus.LOCKED, e.getMessage(), e);
    } catch (WrongLoginOrPasswordException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage(), e);
    } catch (Throwable t) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, t.getMessage(), t);
    }
  }

  @PostMapping("/payment")
  public @ResponseBody
  ResponseEntity<BigDecimal> payment(
      HttpServletRequest request,
      HttpSession session,
      HttpServletResponse response) {
    String token = Utils.getTokenFromRequest(request);
    if (token != null) {
      try {
        BigDecimal balance = customerService.payment(token);
        session.setAttribute(Const.TOKEN_ATTR, token);
        Utils.setTokenToResponse(response, token);
        return ResponseEntity.status(HttpStatus.OK).body(balance);
      } catch (CustomerIsBlockedException e) {
        throw new ResponseStatusException(HttpStatus.LOCKED, e.getMessage(), e);
      } catch (InvalidAuthTokenException e) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage(), e);
      } catch (InsufficientFundsException e1) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e1.getMessage(), e1);
      } catch (Throwable t) {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, t.getMessage(), t);
      }
    }
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Auth token not found");
  }

  @GetMapping("/logout")
  public @ResponseBody
  ResponseEntity<Void> logout(HttpServletRequest request) {
    String token = Utils.getTokenFromRequest(request);
    if (token != null) {
      try {
        customerService.logoutByToken(token);
        request.getSession().removeAttribute(Const.TOKEN_ATTR);
        return ResponseEntity.status(HttpStatus.OK).build();
      } catch (InvalidAuthTokenException e) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage(), e);
      } catch (Throwable t) {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, t.getMessage(), t);
      }
    }
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Auth token not found");
  }
}
