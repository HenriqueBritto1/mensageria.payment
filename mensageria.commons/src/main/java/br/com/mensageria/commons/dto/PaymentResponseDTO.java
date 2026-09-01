package br.com.mensageria.commons.dto;

public record PaymentResponseDTO(
        String amount,
        String date_of_expiration,
        PaymentMethodResponseDTO payment_method
) {
}
