package com.example.gatewayservice.trace;

import org.slf4j.MDC;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class TraceIdWebFilter implements WebFilter {

  private static final String TRACE_ID_HEADER = "X-Trace-Id";

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String traceId = exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER);
    if (traceId == null || traceId.isBlank()) {
      traceId = UUID.randomUUID().toString();
    }

    ServerHttpRequest mutatedRequest = exchange.getRequest()
        .mutate()
        .header(TRACE_ID_HEADER, traceId)
        .build();

    MDC.put(TRACE_ID_HEADER, traceId);

    return chain
        .filter(exchange.mutate().request(mutatedRequest).build())
        .doFinally(signalType -> MDC.remove(TRACE_ID_HEADER));
  }
}