package br.com.mensageria.commons.dto;

import br.com.mensageria.commons.enums.CardFlag;
import br.com.mensageria.commons.enums.TypePayment;
import jakarta.validation.constraints.NotNull;

public record PaymentMethodResponseDTO(
        CardFlag id,
        TypePayment type,
        String statement_descriptor,
        String ticket_url
) {
}
