package br.com.mensageria.api.application.dto;

import br.com.mensageria.commons.enums.CurrencyEnum;
import org.antlr.v4.runtime.misc.NotNull;

import java.math.BigDecimal;

public record PaymentRequestDTO(
        String externalReference,
        BigDecimal amount,
        String currency,
        String callbackUrl
) {
}
