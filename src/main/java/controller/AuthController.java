package controller;

import DTO.AuthRequest;
import DTO.AuthResponse;
import entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.AuthService;
import service.UserImp;
import util.JwtUtil;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

  @Autowired
  private AuthService authService;
  @Autowired
  private UserImp userImp;
  @Autowired
  private JwtUtil jwtil;

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest){
    AuthResponse authResponse = authService.login(authRequest);
    return ResponseEntity.ok(authResponse);
  }

  @PostMapping("/register")
  public ResponseEntity<String> register (@RequestBody User user){
    try{
      userImp.createUser(user);
      return ResponseEntity.ok("User created successfully");
    }catch(Exception e){
      return ResponseEntity.badRequest().body("Registration failed");
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String bearertoken){
    String token = bearertoken.substring(7);
    AuthResponse authResponse = authService.refreshToken(token);
    return ResponseEntity.ok(authResponse);
  }

  @PostMapping("/me")
  public ResponseEntity<User> getCurrentUser(@RequestHeader("Authorization") String bearertoken){
    String token = bearertoken.substring(7);
    String email = jwtil.extractUsername(token);
    User user = userImp.findByEmail(email);
    return ResponseEntity.ok(user);
  }






}
