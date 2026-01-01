package com.teamboard.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {


  @PostConstruct
  public void debug() {
    System.out.println("JWT secret length = " + secret.length());
  }

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration}")
  private long jwtExpiration;

  @Value("${jwt.refreshTokenExpiration:604800000}")
  private long refreshTokenExpiration;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
  }

  public String generateToken(String username){
    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }
  public String generateRefreshToken(String username){
    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();

  }

  public String extractUsername(String token){
    try {
      String subject = Jwts.parserBuilder()
          .setSigningKey(getSigningKey())
          .build()
          .parseClaimsJws(token)
          .getBody()
          .getSubject();
      System.out.println("✅ extractUsername success: " + subject);
      return subject;
    }catch (Exception e){
      System.out.println("❌ extractUsername FAILED: " + e.getMessage());
      e.printStackTrace();
      return null;
    }
  }


  public boolean validateToken(String Token){
    try {
      Jwts.parserBuilder()
          .setSigningKey(getSigningKey())
          .build()
          .parseClaimsJws(Token);
      return true;
    }catch(Exception e){
      return false;
    }
  }

  public boolean isTokenExpired(String token){
    try{
      Date expiration = Jwts.parserBuilder()
          .setSigningKey(getSigningKey())
          .build()
          .parseClaimsJws(token)
          .getBody()
          .getExpiration();
      return expiration.before(new Date());
    }catch(Exception e){
      return true;
    }
  }

}

