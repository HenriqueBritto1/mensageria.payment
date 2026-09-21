package br.com.mensageria.processor.application.mappers;

import br.com.mensageria.commons.dto.PaymentReceiveDTO;
import br.com.mensageria.commons.enums.PaymentStatus;
import br.com.mensageria.processor.application.dto.MercadoPagoRefundResponseDTO;
import br.com.mensageria.processor.application.dto.MercadoPagoResponseDTO;
import br.com.mensageria.processor.infra.entity.Payment;
import br.com.mensageria.processor.infra.entity.PaymentRequest;

public class PaymentMapper {
    public PaymentReceiveDTO montarReceiveDTO(PaymentRequest entity, Payment payment, MercadoPagoResponseDTO mercadoPago) {
        return new PaymentReceiveDTO(
                entity.getId(),
                entity.getCorrelationId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getExternalReference(),
                PaymentStatus.valueOf(mercadoPago.status().toUpperCase()),
                payment.getOrderId(),
                mercadoPago.client_token(),
                entity.getCallbackUrl(),
                mercadoPago.transactions().payments().getFirst().payment_method().ticket_url(),
                entity.getCreatedAt()
        );
    }

    public PaymentReceiveDTO montarReceiveDTORefund(PaymentRequest entity, Payment payment, MercadoPagoRefundResponseDTO mercadoPago) {
        return new PaymentReceiveDTO(
                entity.getId(),
                entity.getCorrelationId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getExternalReference(),
                PaymentStatus.valueOf(mercadoPago.status().toUpperCase()),
                payment.getOrderId(),
                mercadoPago.client_token(),
                entity.getCallbackUrl(),
                null,
                entity.getCreatedAt()
        );
    }
}
