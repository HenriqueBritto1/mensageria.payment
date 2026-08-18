package br.com.mensageria.api.application.dto;

import br.com.mensageria.commons.enums.PaymentStatus;

import java.util.UUID;

public record PaymentResponseDTO(
        UUID transactionId,
        String correlationId,
        PaymentStatus status,
        String mensagem
) {
}
