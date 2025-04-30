package com.example.songservice.trace;

import org.slf4j.MDC;

public class TraceContext {
  private static final ThreadLocal<String> traceIdHolder = new ThreadLocal<>();

  public static void set(String traceId) {
    traceIdHolder.set(traceId);
    MDC.put("traceId", traceId);
  }

  public static String get() {
    return traceIdHolder.get();
  }

  public static void clear() {
    traceIdHolder.remove();
    MDC.remove("traceId");
  }
}
