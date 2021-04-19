package fr.maifvie.thoth.example.configurations;

import akka.actor.ActorSystem;
import akka.kafka.ProducerSettings;
import fr.maif.eventsourcing.EventEnvelope;
import fr.maif.eventsourcing.format.JacksonSimpleFormat;
import fr.maif.eventsourcing.impl.JdbcTransactionManager;
import fr.maif.kafka.JsonSerializer;
import fr.maif.kafka.KafkaSettings;
import fr.maifvie.thoth.example.domain.events.BankEvent;
import fr.maifvie.thoth.example.domain.formats.BankEventFormat;
import io.vavr.Tuple0;
import io.vavr.control.Try;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ThothConfiguration {

    private final String SCHEMA = """
                    CREATE TABLE IF NOT EXISTS ACCOUNTS (
                      id varchar(100) PRIMARY KEY,
                      balance real NOT NULL
                    );
                            
                    CREATE TABLE IF NOT EXISTS bank_journal (
                      id UUID primary key,
                      entity_id varchar(100) not null,
                      sequence_num bigint not null,
                      event_type varchar(100) not null,
                      version int not null,
                      transaction_id varchar(100) not null,
                      event jsonb not null,
                      metadata jsonb,
                      context jsonb,
                      total_message_in_transaction int default 1,
                      num_message_in_transaction int default 1,
                      emission_date timestamp not null default now(),
                      user_id varchar(100),
                      system_id varchar(100),
                      published boolean default false,
                      UNIQUE (entity_id, sequence_num)
                    );
                        
                    CREATE SEQUENCE if not exists bank_sequence_num;
            """;

    private final DatabaseConfiguration databaseConfiguration;
    private final BrokerConfiguration brokerConfiguration;

    @Autowired
    public ThothConfiguration(DatabaseConfiguration databaseConfiguration, BrokerConfiguration brokerConfiguration) {
        this.databaseConfiguration = databaseConfiguration;
        this.brokerConfiguration = brokerConfiguration;
    }

    @Bean
    public ActorSystem actorSystem() {
        return ActorSystem.create();
    }

    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(5);
    }

    @Bean
    public DataSource dataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerName(databaseConfiguration.host);
        dataSource.setPassword(databaseConfiguration.credential.password);
        dataSource.setUser(databaseConfiguration.credential.username);
        dataSource.setDatabaseName(databaseConfiguration.name);
        dataSource.setPortNumber(databaseConfiguration.port);
        return dataSource;
    }

    @Bean
    public JdbcTransactionManager jdbcTransactionManager(DataSource dataSource, ExecutorService executorService) {
        return new JdbcTransactionManager(dataSource, executorService);
    }

    @Bean
    public KafkaSettings settings() {
        return KafkaSettings.newBuilder(String.format("%s:%d", brokerConfiguration.host, brokerConfiguration.port)).build();
    }

    @Bean
    public BankEventFormat bankEventFormat() {
        return new BankEventFormat();
    }

    @Bean
    public ProducerSettings<String, EventEnvelope<BankEvent, Tuple0, Tuple0>> producerSettings(
            ActorSystem actorSystem,
            KafkaSettings kafkaSettings) {
        return kafkaSettings.producerSettings(actorSystem, JsonSerializer.of(
                bankEventFormat(),
                JacksonSimpleFormat.empty(),
                JacksonSimpleFormat.empty()
            )
        );
    }

    @PostConstruct
    public void init() {
        Try.of(() -> dataSource().getConnection().prepareStatement(SCHEMA).execute()).getOrElseThrow(e -> new IllegalStateException("Cannot create schema...", e));
    }

}
