package br.com.mensageria.processor.application.dto;

import java.util.List;

public record RefundRequestDTO(
        List<TransactionRequestDTO> transactions
) {
}
