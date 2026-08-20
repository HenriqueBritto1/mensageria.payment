package br.com.mensageria.api.application.dto;

import br.com.mensageria.commons.enums.CurrencyEnum;
import br.com.mensageria.commons.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentReceiveDTO(
        UUID id,
        String correlationId,
        BigDecimal amount,
        CurrencyEnum currency,
        String externalReference,
        PaymentStatus status,
        String orderId,
        String callbackUrl,
        OffsetDateTime createdAt
) {
}
