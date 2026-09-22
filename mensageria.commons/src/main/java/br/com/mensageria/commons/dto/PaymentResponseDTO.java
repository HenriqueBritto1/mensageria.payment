package br.com.mensageria.commons.dto;

public record PaymentResponseDTO(
        String id,
        String amount,
        String paid_amount,
        String taxes_amount,
        String date_of_expiration,
        PaymentMethodResponseDTO payment_method
) {
}
