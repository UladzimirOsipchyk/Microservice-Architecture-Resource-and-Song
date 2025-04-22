package com.example.gatewayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;

@Configuration
public class GatewayConfig {
  @Bean
  RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
    return builder.routes()
        .route(r -> r.path("/resources/**")
            .uri("lb://RESOURCESERVICE"))
        .route(r -> r.path("/songs/**")
            .uri("lb://SONGSERVICE"))
        .build();
  }
}
