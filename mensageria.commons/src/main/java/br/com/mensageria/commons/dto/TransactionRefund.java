package br.com.mensageria.commons.dto;

import java.util.List;

public record TransactionRefund(
        List<RefundsDTO> refunds
) {
}
