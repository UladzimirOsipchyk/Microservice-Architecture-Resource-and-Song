package com.example.gatewayservice.filter;

import com.example.gatewayservice.trace.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class TraceIdGatewayFilter implements GlobalFilter, Ordered {

  private static final Logger log = LoggerFactory.getLogger(TraceIdGatewayFilter.class);

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String traceId = exchange.getRequest().getHeaders().getFirst("X-Trace-Id");

    if (traceId == null || traceId.isBlank()) {
      traceId = UUID.randomUUID().toString();
    }

    TraceContext.set(traceId);
    exchange.getResponse().getHeaders().add("X-Trace-Id", traceId);

    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
        .header("X-Trace-Id", traceId)
        .build();

    return chain.filter(exchange.mutate().request(mutatedRequest).build())
        .doFinally(signalType -> TraceContext.clear());
  }

  @Override
  public int getOrder() {
    return -1;
  }
}
