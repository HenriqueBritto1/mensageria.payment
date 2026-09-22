package br.com.mensageria.api.application.dto;

import br.com.mensageria.commons.dto.Payer;
import br.com.mensageria.commons.dto.PaymentMethodDTO;
import jakarta.validation.constraints.NotNull;


import java.math.BigDecimal;

public record PaymentRequestDTO(
        @NotNull(message = "valor inválido!")
        BigDecimal total_amount,
        String currency,
        String callbackUrl,
        @NotNull(message = "Informações faltantes do pagador")
        Payer payer,
        @NotNull(message = "Informações faltantes do método de pagamento")
        PaymentMethodDTO payment_method
) {
}
