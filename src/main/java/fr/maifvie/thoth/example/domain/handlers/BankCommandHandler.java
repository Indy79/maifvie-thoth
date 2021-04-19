package fr.maifvie.thoth.example.domain.handlers;

import fr.maif.eventsourcing.CommandHandler;
import fr.maif.eventsourcing.Events;
import fr.maifvie.thoth.example.domain.commands.BankCommand;
import fr.maifvie.thoth.example.domain.events.BankEvent;
import fr.maifvie.thoth.example.domain.states.Account;
import io.vavr.collection.List;
import io.vavr.concurrent.Future;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;

import static fr.maifvie.thoth.example.domain.commands.BankCommand.*;
import static io.vavr.API.*;

public class BankCommandHandler implements CommandHandler<String, Account, BankCommand, BankEvent, List<String>, Connection> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BankCommandHandler.class);

    @Override
    public Future<Either<String, Events<BankEvent, List<String>>>> handleCommand(
            Connection transactionContext,
            Option<Account> previousState,
            BankCommand command) {
        return Future.of(() -> Match(command).of(
                Case($Withdraw(), withdraw -> this.handleWithdraw(previousState, withdraw)),
                Case($Deposit(), deposit -> this.handleDeposit(previousState, deposit)),
                Case($OpenAccount(), this::handleOpening)
        ));
    }

    private Either<String, Events<BankEvent, List<String>>> handleOpening(BankCommand.OpenAccount opening) {
        if (opening.initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            return Left("Initial balance can't be negative");
        }
        String newId = opening.id.get();
        List<BankEvent> events = List(new BankEvent.AccountOpened(newId));
        if (opening.initialBalance.compareTo(BigDecimal.ZERO) > 0) {
            events = events.append(new BankEvent.MoneyDeposited(newId, opening.initialBalance));
        }
        return Right(Events.events(List.empty(), events));
    }

    private Either<String, Events<BankEvent, List<String>>> handleWithdraw(
            Option<Account> previousState,
            BankCommand.Withdraw withdraw) {
        return previousState.toEither("Account does not exist")
                .flatMap(previous -> {
                    BigDecimal newBalance = previous.balance.subtract(withdraw.amount);
                    List<String> messages = List();
                    if(newBalance.compareTo(BigDecimal.ZERO) < 0) {
                        messages = messages.push("warning overdrawing account");
                        if (newBalance.compareTo(BigDecimal.valueOf(120l).negate()) < 0) {
                            return Left("Cannot overdraw account more than 120");
                        }
                    }
                    return Right(Events.events(messages, new BankEvent.MoneyWithdrawn(withdraw.account, withdraw.amount)));
                });
    }

    private Either<String, Events<BankEvent, List<String>>> handleDeposit(
            Option<Account> previousState,
            BankCommand.Deposit deposit) {
        return previousState.toEither("Account does not exist")
                .flatMap(previous -> Right(Events.events(List.empty(), new BankEvent.MoneyDeposited(deposit.account, deposit.amount))));
    }

}
