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

    try{
      String token = extractToken(request);
      if(token != null && jwtUtil.validateToken(token)){
        String username = jwtUtil.extractUsername(token);
        var userDetails = userDetailsService.loadUserByUsername(username);

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
      }
    }catch(Exception e){
      logger.error("Could not set user authentication in security context");
    }
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
