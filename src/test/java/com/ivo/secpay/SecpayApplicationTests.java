package com.ivo.secpay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;

//@ExtendWith(SpringExtension.class)
//@SpringBootTest//(classes = {SecpayApplication.class, SpringJdbcConfig.class})
//@WebMvcTest(PaymentController.class)
//@ExtendWith(SpringExtension.class)
//@WebAppConfiguration()
//@ContextConfiguration(classes = {SecpayApplication.class, SpringJdbcConfig.class})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SecpayApplicationTests {

  @Autowired
  DataSource dataSource;

  @Autowired
  TestRestTemplate restTemplate;

  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() {
    if (jdbcTemplate == null) {
      jdbcTemplate = new JdbcTemplate(dataSource);
    }
    jdbcTemplate.execute("TRUNCATE TABLE customer");
  }

  @Test
  void testController() {
    String object = restTemplate.getForObject("/", String.class);
    System.out.println(object);
    String user1 = "user1", pass1 = "pass1";
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("login", user1);
    map.add("password", pass1);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

    ResponseEntity<String> resp = restTemplate.postForEntity("/register", request, String.class);
    assertEquals(HttpStatus.CREATED, resp.getStatusCode());
    String token = resp.getBody();
    assertNotNull(token);
    checkTokenInHeaders(resp, token);

    resp = restTemplate.postForEntity("/login", request, String.class);
    assertEquals(HttpStatus.OK, resp.getStatusCode());
    assertEquals(token, resp.getBody());
    checkTokenInHeaders(resp, token);

    HttpHeaders headers1 = new HttpHeaders();
    headers1.add(Const.TOKEN_ATTR, token);
    HttpEntity<Void> request1 = new HttpEntity<>(headers1);
    ResponseEntity<BigDecimal> resp1 = restTemplate
        .postForEntity("/payment", request1, BigDecimal.class);
    assertEquals(HttpStatus.OK, resp1.getStatusCode());
    BigDecimal body = resp1.getBody();
    assertNotNull(body);
    assertEquals(BigDecimal.valueOf(6.9).doubleValue(), body.doubleValue());
    checkTokenInHeaders(resp1, token);

    HttpHeaders headers2 = new HttpHeaders();
    headers2.add(Const.TOKEN_ATTR, token);
    HttpEntity<Void> request2 = new HttpEntity<>(headers2);
    ResponseEntity<Void> resp2 = restTemplate
        .exchange("/logout", HttpMethod.GET, request2, Void.class);
    assertEquals(HttpStatus.OK, resp2.getStatusCode());
    assertNull(resp2.getHeaders().get(Const.TOKEN_ATTR));

    assertThrows(RestClientException.class,
        () -> restTemplate.postForEntity("/payment", request1, BigDecimal.class));
  }

  private void checkTokenInHeaders(ResponseEntity<?> resp, final String token) {
    List<String> cookie = resp.getHeaders().get(HttpHeaders.SET_COOKIE);
    assert cookie != null;
    String c = cookie.stream().filter(s -> s.startsWith(Const.TOKEN_ATTR)).findFirst()
        .orElseThrow(AssertionError::new);
    assertTrue(c.contains(token));
    List<String> tokenList = resp.getHeaders().get(Const.TOKEN_ATTR);
    assert tokenList != null;
    tokenList.stream().filter(token::equals).findFirst().orElseThrow(AssertionError::new);
  }

}
