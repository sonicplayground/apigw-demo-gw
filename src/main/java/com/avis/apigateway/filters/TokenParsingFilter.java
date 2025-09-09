package com.avis.apigateway.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class TokenParsingFilter extends
    AbstractGatewayFilterFactory<TokenParsingFilter.Config> {

  final long THIRTY_MINUTES_MILLIS = 30 * 60 * 1000L;

  public TokenParsingFilter() {
    super(Config.class);
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      String token = exchange.getRequest().getHeaders().getFirst("Authorization");
      log.info("Authorization header: {}", token);
      if (token == null || token.isEmpty()) {
        log.warn("No Authorization header found");
        return handleUnauthorized(exchange, "Missing Authorization header");
      }

      String[] tokenParts = token.split("-");
      if (tokenParts.length < 3) {
        log.warn("Invalid token format");
        return handleUnauthorized(exchange, "Invalid token format");
      }

      String userId = tokenParts[1];

      // timestamp 검증 (30분 유효성)
      try {
        long timeDiff = System.currentTimeMillis() - Long.parseLong(tokenParts[2]);
        if (timeDiff > THIRTY_MINUTES_MILLIS) {
          log.warn("Token expired");
          return handleUnauthorized(exchange, "Token expired");
        }
      } catch (NumberFormatException e) {
        log.warn("Invalid timestamp format: {}", tokenParts[2]);
        return handleUnauthorized(exchange, "Invalid timestamp");
      }

      log.info("Extracted userId: {}", userId);
      ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
          .header("X-User-Id", userId)
          .build();

      ServerWebExchange modifiedExchange = exchange.mutate()
          .request(modifiedRequest)
          .build();

      return chain.filter(modifiedExchange);
    };
  }

  private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    exchange.getResponse().getHeaders().add("Content-Type", "application/json");

    String body = "{\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}";
    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes());

    return exchange.getResponse().writeWith(Mono.just(buffer));
  }

  public static class Config {
    // 빈 Config 클래스 - 설정값이 필요 없어도 반드시 있어야 함
  }
}
