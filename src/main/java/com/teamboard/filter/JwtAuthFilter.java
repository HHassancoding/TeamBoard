package com.teamboard.filter;

import com.teamboard.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.teamboard.util.JwtUtil;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  @Autowired
  private CustomUserDetailsService userDetailsService;


  private final JwtUtil jwtUtil;

  @Autowired
  JwtAuthFilter(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }


  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

    String requestURI = request.getRequestURI();
    String method = request.getMethod();

    // Skip JWT validation for CORS preflight requests
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      System.out.println("⏭ Skipping JWT validation for OPTIONS preflight: " + requestURI);
      filterChain.doFilter(request, response);
      return;
    }

    System.out.println("========================================");
    System.out.println("JWT FILTER START");
    System.out.println("========================================");
    System.out.println("Request URI: " + requestURI);
    System.out.println("Method: " + method);

    String authHeader = request.getHeader("Authorization");
    System.out.println("Has Authorization Header: " + (authHeader != null));

    if (authHeader != null) {
      System.out.println("Authorization Header Starts With 'Bearer ': " + authHeader.startsWith("Bearer "));
      if (authHeader.startsWith("Bearer ")) {
        String tokenPreview = authHeader.substring(0, Math.min(30, authHeader.length()));
        System.out.println("Token Preview: " + tokenPreview + "...");
      }
    } else {
      System.out.println("⚠ WARNING: No Authorization header present - request will fail authentication");
    }

    try{
      String token = extractToken(request);
      System.out.println("Token extracted: " + (token != null));

      if(token != null){
        System.out.println("Step 1: Validating token...");
        boolean isValid = jwtUtil.validateToken(token);
        System.out.println("Token validation result: " + isValid);

        if(isValid){
          System.out.println("Step 2: Extracting username from token...");
          String username = jwtUtil.extractUsername(token);
          System.out.println("Username extracted: " + username);

          if(username != null) {
            System.out.println("Step 3: Loading user details for: " + username);
            var userDetails = userDetailsService.loadUserByUsername(username);
            System.out.println("User details loaded: " + userDetails.getUsername());
            System.out.println("User authorities: " + userDetails.getAuthorities());

            System.out.println("Step 4: Creating authentication token...");
            UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
            authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("✅ Authentication set successfully in SecurityContext");
          } else {
            System.out.println("❌ Username extraction failed - null username");
          }
        } else {
          System.out.println("❌ Token validation FAILED - token is invalid or expired");
        }
      } else {
        System.out.println("❌ No token extracted from Authorization header");
      }
    }catch(Exception e){
      System.out.println("========================================");
      System.out.println("❌ EXCEPTION CAUGHT IN JWT FILTER");
      System.out.println("Exception Type: " + e.getClass().getName());
      System.out.println("Exception Message: " + e.getMessage());
      System.out.println("Stack trace:");
      e.printStackTrace();
      System.out.println("========================================");
    }

    System.out.println("JWT Filter completed - Passing to next filter");
    System.out.println("Current Authentication: " + SecurityContextHolder.getContext().getAuthentication());
    System.out.println("========================================");

    filterChain.doFilter(request, response);
  }


  private String extractToken(HttpServletRequest request){
    String bearer = request.getHeader("Authorization");
    if(bearer != null && bearer.startsWith("Bearer ")){
      return bearer.substring(7);
    }
    return null;
  }
}
