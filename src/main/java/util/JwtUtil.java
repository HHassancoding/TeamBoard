package util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration}")
  private String jtwExpiration;

  @Value("${jwt.refreshTokenExpiration}")
  private long refreshTokenExpiration;

  private SecretKey getSigningKey(){
    return Keys.hmacShaKeyFor( secret.getBytes());
  }

  public String generateToken(String username){
    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + jtwExpiration))
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
      return Jwts.parserBuilder()
          .setSigningKey(getSigningKey())
          .build()
          .parseClaimsJws(token)
          .getBody()
          .getSubject();
    }catch (Exception e){
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

