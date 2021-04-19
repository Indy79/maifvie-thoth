package fr.maifvie.thoth.example.application.models;

import java.math.BigDecimal;

public class AccountDto {
    private final String id;
    private final BigDecimal balance;

    public AccountDto(String id, BigDecimal balance) {
        this.id = id;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

}
