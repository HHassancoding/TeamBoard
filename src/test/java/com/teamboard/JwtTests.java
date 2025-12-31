package com.teamboard;

import com.teamboard.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class JwtTests {

  private String token;
  private final String username = "test";

  @Autowired
  private JwtUtil jwtUtil;

  @BeforeEach
  void setUp() {
    token = jwtUtil.generateToken(username);
  }
  // Test 1
  @Test
  public void testGenerateToken() {
    assertNotNull(token);
    assertTrue(token.contains("."));
  }

  // Test 2
  @Test
  public void extractUsernameTest() {
    assertEquals(username, jwtUtil.extractUsername(token));
  }

  // Test 3
  @Test
  public void validateTokenTest(){
    boolean valid = jwtUtil.validateToken(token);
    assertTrue(valid);
  }

  // Test 4
  @Test
  public void ExpiredTokenTest(){
    boolean valid = jwtUtil.isTokenExpired(token);
    assertFalse(valid);
  }

  // Test 5
  @Test
  public void refreshTokenTest(){
    String refreshToken = jwtUtil.generateRefreshToken(username);
    assertNotNull(refreshToken);
    assertTrue(jwtUtil.validateToken(refreshToken));
  }

}
