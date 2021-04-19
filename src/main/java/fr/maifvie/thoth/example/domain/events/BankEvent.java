package fr.maifvie.thoth.example.domain.events;

import fr.maif.eventsourcing.Event;
import fr.maif.eventsourcing.Type;
import io.vavr.API.Match.Pattern0;

import java.math.BigDecimal;

public abstract class BankEvent implements Event {
    public static Type<AccountOpened> AccountOpenedV1 = Type.create(AccountOpened.class, 1L);
    public static Type<MoneyWithdrawn> MoneyWithdrawnV1 = Type.create(MoneyWithdrawn.class, 1L);
    public static Type<MoneyDeposited> MoneyDepositedV1 = Type.create(MoneyDeposited.class, 1L);

    /**
     * Boilerplate code to facilitate pattern matching
     */
    public static Pattern0<AccountOpened> $AccountOpened() {
        return Pattern0.of(AccountOpened.class);
    }
    public static Pattern0<MoneyWithdrawn> $MoneyWithdrawn() {
        return Pattern0.of(MoneyWithdrawn.class);
    }
    public static Pattern0<MoneyDeposited> $MoneyDeposited() {
        return Pattern0.of(MoneyDeposited.class);
    }


    protected final String accountId;

    public BankEvent(String accountId) {
        this.accountId = accountId;
    }

    @Override
    public String entityId() {
        return accountId;
    }

    public static class AccountOpened extends BankEvent {
        public AccountOpened(String id) {
            super(id);
        }

        @Override
        public Type<?> type() {
            return AccountOpenedV1;
        }
    }

    public static class MoneyWithdrawn extends BankEvent {
        public final BigDecimal amount;

        public MoneyWithdrawn(String id, BigDecimal amount) {
            super(id);
            this.amount = amount;
        }

        @Override
        public Type<?> type() {
            return MoneyWithdrawnV1;
        }
    }

    public static class MoneyDeposited extends BankEvent {
        public final BigDecimal amount;

        public MoneyDeposited(String id, BigDecimal amount) {
            super(id);
            this.amount = amount;
        }

        @Override
        public Type<?> type() {
            return MoneyDepositedV1;
        }
    }

}

