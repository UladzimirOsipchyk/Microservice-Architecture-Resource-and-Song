package com.example.gatewayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    return http
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .authorizeExchange(exchange -> exchange
            .pathMatchers(HttpMethod.GET, "/storage/**").hasAnyAuthority("SCOPE_read", "SCOPE_write")
            .pathMatchers(HttpMethod.POST, "/storage/**").hasAuthority("SCOPE_write")
            .pathMatchers(HttpMethod.DELETE, "/storage/**").hasAuthority("SCOPE_write")
            .pathMatchers("/oauth2/token").permitAll()
            .anyExchange().authenticated()
        )
        .oauth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec::jwt)
        .build();
  }
}
