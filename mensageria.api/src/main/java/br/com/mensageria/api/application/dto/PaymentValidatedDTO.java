package br.com.mensageria.api.application.dto;

import java.util.UUID;

public record PaymentValidatedDTO(
        UUID pagamentoId,
        String correlationId
) {
}
