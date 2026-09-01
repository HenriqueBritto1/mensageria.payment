package br.com.mensageria.commons.dto;

import java.util.List;

public record TransactionsResponse(
        List<PaymentResponseDTO> payments
) {
}
