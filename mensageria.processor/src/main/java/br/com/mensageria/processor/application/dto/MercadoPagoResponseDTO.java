package br.com.mensageria.processor.application.dto;

import br.com.mensageria.commons.dto.Transactions;

public record MercadoPagoResponseDTO(
        String id,
        String type,
        String processing_mode,
        String external_reference,
        String total_amount,
        String total_paid_amount,
        String created_date,
        String last_updated_date,
        String country_code,
        String status,
        String status_detail,
        String capture_mode,
        String client_token,
        Transactions transactions
) {
}
