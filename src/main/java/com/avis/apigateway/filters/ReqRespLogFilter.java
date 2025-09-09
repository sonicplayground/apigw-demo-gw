package com.avis.apigateway.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ReqRespLogFilter implements GlobalFilter, Ordered {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    log.info("Pre Filter Logic: Request path: {}", exchange.getRequest().getPath());
    return chain.filter(exchange).then(Mono.fromRunnable(
        () -> log.info("Post Filter Logic: HTTP Status Code: {}",
            exchange.getResponse().getStatusCode())));
  }

  @Override
  public int getOrder() {
    return -1;
  }
}
