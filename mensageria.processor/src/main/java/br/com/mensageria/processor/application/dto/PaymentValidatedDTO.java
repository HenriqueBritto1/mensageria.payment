package br.com.mensageria.processor.application.dto;

import java.util.UUID;

public record PaymentValidatedDTO(
        UUID pagamentoId,
        String correlationId
) {
}
