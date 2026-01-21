package com.teamboard.config;

import com.teamboard.filter.JwtAuthFilter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;

  @Autowired
  public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
    this.jwtAuthFilter = jwtAuthFilter;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
    return config.getAuthenticationManager();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    // Frontend origins
    config.setAllowedOrigins(List.of(
        "http://localhost:5173",
        "http://localhost:3000",
        "https://teamboard-frontend.onrender.com"
    ));

    // Methods including PATCH & OPTIONS preflight
    config.setAllowedMethods(List.of(
        "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
    ));

    // Explicitly allow headers your frontend may send
    config.setAllowedHeaders(List.of(
        "Authorization",
        "Content-Type",
        "Accept",
        "Origin",
        "X-Requested-With"
    ));

    // Expose headers if needed (optional)
    config.setExposedHeaders(List.of(
        "Authorization",
        "Content-Disposition"
    ));

    // Allow credentials (cookies, JWT, etc.)
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return source;
  }



  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) {
    try {
      http
          .cors(cors -> {}) // uses the CorsConfigurationSource bean
          .csrf(AbstractHttpConfigurer::disable)
          .sessionManagement(session ->
              session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(authz -> authz
              .requestMatchers("/api/auth/**").permitAll()
              .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // Allow all CORS preflight
              .requestMatchers("/api/workspaces/**").authenticated()
              .requestMatchers("/api/projects/**").authenticated()
              .requestMatchers("/api/tasks/**").authenticated()
              .anyRequest().authenticated()
              )
          .httpBasic(AbstractHttpConfigurer::disable)
          .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    } catch (Exception e) {
      throw new RuntimeException("Failed to configure security filter chain", e);
    }

    return http.build();
  }
}
