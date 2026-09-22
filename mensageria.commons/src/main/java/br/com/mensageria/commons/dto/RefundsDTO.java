package br.com.mensageria.commons.dto;

public record RefundsDTO(
        String id,
        String transaction_id,
        String amount,
        String status
) {
}
