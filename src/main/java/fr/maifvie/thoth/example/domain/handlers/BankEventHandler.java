package fr.maifvie.thoth.example.domain.handlers;

import fr.maif.eventsourcing.EventHandler;
import fr.maifvie.thoth.example.domain.events.BankEvent;
import fr.maifvie.thoth.example.domain.states.Account;
import io.vavr.control.Option;

import java.math.BigDecimal;

import static fr.maifvie.thoth.example.domain.events.BankEvent.*;
import static io.vavr.API.Case;
import static io.vavr.API.Match;

public class BankEventHandler implements EventHandler<Account, BankEvent> {
    @Override
    public Option<Account> applyEvent(
            Option<Account> previousState,
            BankEvent event) {
        return Match(event).of(
                Case($AccountOpened(), BankEventHandler::handleAccountOpened),
                Case($MoneyDeposited(), deposit -> BankEventHandler.handleMoneyDeposited(previousState, deposit)),
                Case($MoneyWithdrawn(), withdrawn -> BankEventHandler.handleMoneyWithdrawned(previousState, withdrawn))
        );
    }

    private static Option<Account> handleAccountOpened(BankEvent.AccountOpened event) {
        Account account = new Account();
        account.id = event.entityId();
        account.balance = BigDecimal.ZERO;

        return Option.some(account);
    }

    private static Option<Account> handleMoneyDeposited(
            Option<Account> previousState,
            BankEvent.MoneyDeposited event) {
        return previousState.map(state -> {
            state.balance = state.balance.add(event.amount);
            return state;
        });
    }

    private static Option<Account> handleMoneyWithdrawned(
            Option<Account> previousState,
            BankEvent.MoneyWithdrawn event) {
        return previousState.map(state -> {
            state.balance = state.balance.subtract(event.amount);
            return state;
        });
    }

}
