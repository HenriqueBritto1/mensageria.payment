package br.com.mensageria.processor.application.dto;

import br.com.mensageria.commons.dto.Address;
import br.com.mensageria.commons.dto.Payer;
import br.com.mensageria.commons.dto.Phone;
import br.com.mensageria.commons.dto.Transactions;

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
