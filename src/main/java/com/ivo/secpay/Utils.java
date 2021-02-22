package com.ivo.secpay;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.jdbc.core.ResultSetExtractor;

public class Utils {

  public static final ResultSetExtractor<String> STRING_EXTRACTOR = rs -> {
    if (rs.getRow() <= 0) {
      rs.next();
    }
    return rs.getString(1);
  };
  public static final ResultSetExtractor<Integer> INT_EXTRACTOR = rs -> {
    if (rs.getRow() <= 0) {
      rs.next();
    }
    return rs.getInt(1);
  };

  private Utils() {
  }

  public static BigDecimal convertToMoney(int value) {
    return BigDecimal.valueOf(value, 2);
  }

  public static void setTokenToResponse(HttpServletResponse response, String token) {
    Cookie cookie = new Cookie(Const.TOKEN_ATTR, token);
    cookie.setMaxAge(Const.TOKEN_COOKIE_MAX_AGE);
    response.addCookie(cookie);
    response.setHeader(Const.TOKEN_ATTR, token);
  }

  public static String getTokenFromRequest(HttpServletRequest request) {
    String token = (String) request.getSession().getAttribute(Const.TOKEN_ATTR);
    if (token == null) {
      token = request.getHeader(Const.TOKEN_ATTR);
    }
    if (token == null) {
      Optional<Cookie> tokenCookie = Arrays.stream(request.getCookies())
          .filter(cookie -> Const.TOKEN_ATTR.equals(cookie.getName())).findFirst();
      if (tokenCookie.isPresent()) {
        token = tokenCookie.get().getValue();
      }
    }
    return token;
  }
}
