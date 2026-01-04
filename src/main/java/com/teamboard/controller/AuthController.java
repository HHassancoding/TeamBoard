package com.teamboard.controller;

import com.teamboard.DTO.AuthRequest;
import com.teamboard.DTO.AuthResponse;
import com.teamboard.DTO.RegisterDTO;
import com.teamboard.DTO.UserResponseDTO;
import com.teamboard.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.teamboard.service.AuthService;
import com.teamboard.service.UserService;
import com.teamboard.util.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  @Autowired
  private AuthService authService;
  @Autowired
  private UserService userService;
  @Autowired
  private JwtUtil jwtil;

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest){
    AuthResponse authResponse = authService.login(authRequest);
    return ResponseEntity.ok(authResponse);
  }

  @PostMapping("/register")
  public ResponseEntity<String> register (@RequestBody RegisterDTO registerDTO){
    try{
      userService.createUser(registerDTO);
      return ResponseEntity.ok("User created successfully");
    }catch(Exception e){
      return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String bearertoken){
    String token = bearertoken.substring(7);
    AuthResponse authResponse = authService.refreshToken(token);
    return ResponseEntity.ok(authResponse);
  }

  @GetMapping("/me")
  public ResponseEntity<UserResponseDTO> getCurrentUser(@RequestHeader("Authorization") String bearertoken){
    try{
      String token = bearertoken.substring(7);
      String email = jwtil.extractUsername(token);
      User user = userService.findByEmail(email);
      if(user==null) return ResponseEntity.status(401).build();
      return ResponseEntity.ok(mapToUserResponse(user));
    }catch(Exception e){
      return ResponseEntity.status(401).build();
    }
  }

  private UserResponseDTO mapToUserResponse(User user) {
    return UserResponseDTO.builder()
        .id(user.getId())
        .name(user.getName())
        .email(user.getEmail())
        .avatarInitials(user.getAvatarInitials())
        .build();
  }

}
