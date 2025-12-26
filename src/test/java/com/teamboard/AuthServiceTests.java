package com.teamboard;


import com.teamboard.DTO.AuthRequest;
import com.teamboard.DTO.AuthResponse;
import com.teamboard.entity.User;
import com.teamboard.service.AuthService;
import com.teamboard.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTests {

  @Mock
  private JwtUtil jwtUtil;

  @Mock
  private AuthenticationManager authenticationManager;

  @InjectMocks
  private AuthService authService;

  private AuthRequest authRequest;
  private User user;

  @BeforeEach
  void setUp() {
    authRequest = new AuthRequest("test@example.com", "password123");

    user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    user.setName("Test User");
    user.setPasswordHash("hashed_password");
  }


  @Test
  public void refreshTokenTest(){
    String oldToken= "old.token.first";
    String newToken = "new.token.second";

    when(jwtUtil.extractUsername(oldToken)).thenReturn("test");
    when(jwtUtil.validateToken(oldToken)).thenReturn(true);
    when(jwtUtil.generateToken("test")).thenReturn(newToken);


    AuthResponse authResponse = authService.refreshToken(oldToken);

    assertEquals(oldToken, authResponse.getRefreshToken());
    assertEquals("test", authResponse.getUsername());
    assertEquals(newToken, authResponse.getAccessToken());
    assertNotNull(authResponse);
    verify(jwtUtil).generateToken("test");
    verify(jwtUtil).validateToken(oldToken);
    assertEquals("86400", authResponse.getExpiresIn());
  }

  @Test
  public void loginTest(){

    String accessToken = "token.generated";
    String refreshToken = "refresh.token.generated";

    Authentication auth = mock(Authentication.class);

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(auth);
    when(jwtUtil.generateToken(authRequest.getEmail())).thenReturn(accessToken);
    when(jwtUtil.generateRefreshToken(authRequest.getEmail())).thenReturn(refreshToken);
    AuthResponse authResponse = authService.login(authRequest);

    assertEquals(accessToken, authResponse.getAccessToken());
    assertEquals("86400", authResponse.getExpiresIn());
    assertNotNull(authResponse);
    assertEquals(authRequest.getEmail(), authResponse.getUsername());
    verify(jwtUtil).generateToken(authRequest.getEmail());




  }

}
