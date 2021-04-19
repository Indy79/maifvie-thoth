package fr.maifvie.thoth.example.application.controllers;

import fr.maifvie.thoth.example.application.models.AccountDto;
import fr.maifvie.thoth.example.application.models.AccountOperationRequest;
import fr.maifvie.thoth.example.domain.Bank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@Component
public class AccountHandler {

    private final Bank bankDomainHandler;

    @Autowired
    public AccountHandler(Bank bankDomainHandler) {
        this.bankDomainHandler = bankDomainHandler;
    }

    public Mono<ServerResponse> readAccount(ServerRequest request) {
        String accountId = request.pathVariable("id");
        return Mono.fromFuture(bankDomainHandler.findAccountById(accountId).toCompletableFuture()).flatMap(accounts -> accounts.map(account ->
                ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(new AccountDto(account.id, account.balance)))
                ).getOrElse(Mono.empty())
        );
    }

    public Mono<ServerResponse> createAccount(ServerRequest request) {
        return Mono.fromFuture(bankDomainHandler.createAccount(BigDecimal.ZERO).toCompletableFuture())
                .flatMap(eitherSuccess ->
                        eitherSuccess.map(ps ->
                                ps.currentState
                                        .map(account -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                                                .body(BodyInserters.fromValue(new AccountDto(account.id, account.balance))))
                                        .getOrElse(() -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
                        )
                                .getOrElseGet((e) -> ServerResponse.badRequest().body(BodyInserters.fromValue(CommonError.builder().errors(List.of(e)).build())))
                );
    }
    public Mono<ServerResponse> withdrawFromAccount(ServerRequest request) {
        String accountId = request.pathVariable("id");
        return request.body(BodyExtractors.toMono(AccountOperationRequest.class)).flatMap(accountOperationRequest -> {
            switch (accountOperationRequest.getType()) {
                case WITHDRAW -> {
                    return Mono.fromFuture(bankDomainHandler.withdraw(accountId, accountOperationRequest.getAmount()).toCompletableFuture())
                            .flatMap(eitherSuccess ->
                                    eitherSuccess.map(ps ->
                                            ps.currentState
                                                    .map(account -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                                                            .body(BodyInserters.fromValue(new AccountDto(account.id, account.balance))))
                                                    .getOrElse(() -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
                                    )
                                            .getOrElseGet((e) -> ServerResponse.badRequest().body(BodyInserters.fromValue(CommonError.builder().errors(List.of(e)).build())))
                            );
                }
                case DEPOSIT -> {
                    return Mono.fromFuture(bankDomainHandler.deposit(accountId, accountOperationRequest.getAmount()).toCompletableFuture())
                            .flatMap(eitherSuccess ->
                                    eitherSuccess.map(ps ->
                                            ps.currentState
                                                    .map(account -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                                                            .body(BodyInserters.fromValue(new AccountDto(account.id, account.balance))))
                                                    .getOrElse(() -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
                                    )
                                            .getOrElseGet((e) -> ServerResponse.badRequest().body(BodyInserters.fromValue(CommonError.builder().errors(List.of(e)).build())))
                            );
                }
                default -> {
                    return ServerResponse.badRequest().build();
                }
            }
        });

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CommonError {
        private List<String> errors;
    }

}
