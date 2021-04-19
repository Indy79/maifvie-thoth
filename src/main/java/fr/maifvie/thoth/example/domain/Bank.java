package fr.maifvie.thoth.example.domain;

import akka.actor.ActorSystem;
import akka.kafka.ProducerSettings;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import fr.maif.eventsourcing.EventEnvelope;
import fr.maif.eventsourcing.EventProcessor;
import fr.maif.eventsourcing.PostgresKafkaEventProcessor;
import fr.maif.eventsourcing.ProcessingSuccess;
import fr.maif.eventsourcing.impl.JdbcTransactionManager;
import fr.maif.eventsourcing.impl.TableNames;
import fr.maifvie.thoth.example.DemoApplication;
import fr.maifvie.thoth.example.domain.commands.BankCommand;
import fr.maifvie.thoth.example.domain.events.BankEvent;
import fr.maifvie.thoth.example.domain.formats.BankEventFormat;
import fr.maifvie.thoth.example.domain.handlers.BankCommandHandler;
import fr.maifvie.thoth.example.domain.handlers.BankEventHandler;
import fr.maifvie.thoth.example.domain.states.Account;
import fr.maifvie.thoth.example.infrastructure.aggregates.BankAggregateStore;
import fr.maifvie.thoth.example.infrastructure.projections.MeanWithdrawProjection;
import io.vavr.Lazy;
import io.vavr.Tuple0;
import io.vavr.collection.List;
import io.vavr.concurrent.Future;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.concurrent.ExecutorService;

@Component
public class Bank {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bank.class);

    private final EventProcessor<String, Account, BankCommand, BankEvent, Connection, List<String>, Tuple0, Tuple0> eventProcessor;
    private final MeanWithdrawProjection meanWithdrawProjection;
    private static final TimeBasedGenerator UUIDgenerator = Generators.timeBasedGenerator();
    private final BankCommandHandler commandHandler = new BankCommandHandler();
    private final BankEventHandler eventHandler = new BankEventHandler();
    private TableNames tableNames() {
        return new TableNames("bank_journal", "bank_sequence_num");
    }

    @Autowired
    public Bank(
            ActorSystem actorSystem,
            DataSource dataSource,
            JdbcTransactionManager jdbcTransactionManager,
            ExecutorService executorService,
            BankEventFormat bankEventFormat,
            ProducerSettings<String, EventEnvelope<BankEvent, Tuple0, Tuple0>> producerSettings
    ) {
        String topic = "bank";

        TableNames tableNames = tableNames();

        this.meanWithdrawProjection = new MeanWithdrawProjection();

        this.eventProcessor = PostgresKafkaEventProcessor
                .withActorSystem(actorSystem)
                .withDataSource(dataSource)
                .withTables(tableNames)
                .withTransactionManager(jdbcTransactionManager, executorService)
                .withEventFormater(bankEventFormat)
                .withNoMetaFormater()
                .withNoContextFormater()
                .withKafkaSettings(topic, producerSettings)
                .withEventHandler(eventHandler)
                .withAggregateStore(builder -> new BankAggregateStore(
                        builder.eventStore,
                        builder.eventHandler,
                        builder.system,
                        builder.transactionManager
                ))
                .withCommandHandler(commandHandler)
                .withProjections(meanWithdrawProjection)
                .build();
    }

    public Future<Either<String, ProcessingSuccess<Account, BankEvent, Tuple0, Tuple0, List<String>>>> createAccount(
            BigDecimal amount) {
        Lazy<String> lazyId = Lazy.of(() -> UUIDgenerator.generate().toString());
        return eventProcessor.processCommand(new BankCommand.OpenAccount(lazyId, amount));
    }

    public Future<Either<String, ProcessingSuccess<Account, BankEvent, Tuple0, Tuple0, List<String>>>> withdraw(
            String account, BigDecimal amount) {
        LOGGER.debug("Withdraw account : {}", account);
        return eventProcessor.processCommand(new BankCommand.Withdraw(account, amount));
    }

    public Future<Either<String, ProcessingSuccess<Account, BankEvent, Tuple0, Tuple0, List<String>>>> deposit(
            String account, BigDecimal amount) {
        LOGGER.debug("Deposit account : {}", account);
        return eventProcessor.processCommand(new BankCommand.Deposit(account, amount));
    }

    public Future<Option<Account>> findAccountById(String id) {
        return eventProcessor.getAggregate(id);
    }

    public BigDecimal meanWithdrawValue() {
        return meanWithdrawProjection.meanWithdraw();
    }

}
