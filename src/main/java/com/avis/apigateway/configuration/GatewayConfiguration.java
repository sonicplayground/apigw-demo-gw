package com.avis.apigateway.configuration;

import com.avis.apigateway.service.RouteService;
import com.avis.apigateway.service.impl.ApiRouteLocatorImpl;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfiguration {

  @Bean
  public RouteLocator routeLocator(RouteService routeService, RouteLocatorBuilder routeLocationBuilder) {
    return new ApiRouteLocatorImpl(routeLocationBuilder, routeService);
  }
}