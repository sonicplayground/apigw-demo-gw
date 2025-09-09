package com.avis.apigateway.repository;

import com.avis.apigateway.entity.ApiRoute;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends ReactiveCrudRepository<ApiRoute, String> {
}