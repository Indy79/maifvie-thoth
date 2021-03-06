package fr.maifvie.thoth.example.domain.formats;

import com.fasterxml.jackson.databind.JsonNode;
import fr.maif.eventsourcing.format.JacksonEventFormat;
import fr.maif.json.Json;
import fr.maif.json.JsonWrite;
import fr.maifvie.thoth.example.domain.events.BankEvent;
import io.vavr.API;
import io.vavr.Tuple;
import io.vavr.control.Either;

import static io.vavr.API.Case;

public class BankEventFormat implements JacksonEventFormat<String, BankEvent> {
    @Override
    public Either<String, BankEvent> read(String type, Long version, JsonNode json) {
        return API.Match(Tuple.of(type, version)).option(
                Case(BankEvent.MoneyDepositedV1.pattern2(), () -> Json.fromJson(json, BankEvent.MoneyDeposited.class)),
                Case(BankEvent.MoneyWithdrawnV1.pattern2(), () -> Json.fromJson(json, BankEvent.MoneyWithdrawn.class)),
                Case(BankEvent.AccountOpenedV1.pattern2(), () -> Json.fromJson(json, BankEvent.AccountOpened.class))
        )
                .toEither(() -> "Unknown event type " + type + "(v" + version + ")")
                .flatMap(jsResult -> jsResult.toEither().mapLeft(errs -> errs.mkString(",")));
    }

    @Override
    public JsonNode write(BankEvent event) {
        return Json.toJson(event, JsonWrite.auto());
    }
}
