package fr.maifvie.thoth.example.application.controllers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class AccountRouter {

    @Bean
    public RouterFunction<ServerResponse> route(AccountHandler accountHandler) {
        return RouterFunctions
                .route(RequestPredicates.POST("/accounts").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), accountHandler::createAccount)
                .andRoute(RequestPredicates.GET("/accounts/{id}").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), accountHandler::readAccount)
                .andRoute(RequestPredicates.POST("/accounts/{id}/operations").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), accountHandler::withdrawFromAccount);
    }

}
