package fr.maifvie.thoth.example.application.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountOperationRequest {

    private OperationTypeEnum type;
    private TransfertMetadata transfert;
    private BigDecimal amount;

}
