package br.com.mensageria.commons.dto;

import java.util.UUID;

public record PaymentValidatedDTO(
        UUID pagamentoId,
        String correlationId,
        Payer payer,
        Transactions transactions,
        Phone phone,
        Address adress
) {
}
