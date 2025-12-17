package service;

import DTO.AuthRequest;
import DTO.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import repository.UserRepo;
import util.JwtUtil;

@Service
public class AuthService {

  @Autowired
  private UserRepo userRepo;
  @Autowired
  private JwtUtil jwtUtil;
  @Autowired
  private AuthenticationManager authenticationManager;


  public AuthResponse login (AuthRequest authRequest){
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            authRequest.getEmail(),
            authRequest.getPassword())
    );

    SecurityContextHolder
        .getContext()
        .setAuthentication(authentication);
    String accessToken = jwtUtil.generateToken(authRequest.getEmail());
    String refreshToken = jwtUtil.generateRefreshToken(authRequest.getEmail());

    return AuthResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .username(authRequest.getEmail())
        .expiresIn("86400")
        .build();
  }

  public AuthResponse refreshToken(String refreshToken){
    if(jwtUtil.validateToken(refreshToken)){
      String username = jwtUtil.extractUsername(refreshToken);
      String accessToken = jwtUtil.generateToken(username);

      return AuthResponse.builder()
          .accessToken(accessToken)
          .refreshToken(refreshToken)
          .username(username)
          .expiresIn("86400")
          .build();
    }
    throw new RuntimeException("Invalid refresh token");
  }



}
