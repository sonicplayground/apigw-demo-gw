package com.avis.apigateway.handler;

import com.avis.apigateway.configuration.GatewayRoutesRefresher;
import com.avis.apigateway.entity.ApiRoute;
import com.avis.apigateway.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@RequiredArgsConstructor
@Component
public class ApiRouteHandler {
  private final RouteService routeService;

  private final RouteLocator routeLocator;

  private final GatewayRoutesRefresher gatewayRoutesRefresher;

  public Mono<ServerResponse> create(ServerRequest serverRequest) {
    Mono<ApiRoute> apiRoute = serverRequest.bodyToMono(ApiRoute.class);
    return apiRoute.flatMap(route ->
        ServerResponse.status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(routeService.create(route), ApiRoute.class));
  }

  public Mono<ServerResponse> getById(ServerRequest serverRequest) {
    final String apiId = serverRequest.pathVariable("routeId");
    Mono<ApiRoute> apiRoute = routeService.getById(apiId);
    return apiRoute.flatMap(route -> ServerResponse.ok()
            .body(fromValue(route)))
        .switchIfEmpty(ServerResponse.notFound()
            .build());
  }

  public Mono<ServerResponse> refreshRoutes(ServerRequest serverRequest) {
    gatewayRoutesRefresher.refreshRoutes();
    return ServerResponse.ok().body(BodyInserters.fromObject("Routes reloaded successfully"));
  }
}
